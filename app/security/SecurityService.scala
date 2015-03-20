package security

import exceptions.AccessDeniedException
import models.User

import scala.collection.GenTraversable

/**
 * Checks if user can access to some unspecified resource.
 * @param predicate User => Boolean, true if User has access to resource.
 */
class AccessChecker0(predicate: User => Boolean) {

  def hasAccess(implicit user: User): Boolean = predicate(user)

  def || (other: AccessChecker0) = new AccessChecker0 ( (user: User) =>
    this.hasAccess(user) || other.hasAccess(user)
  )
  def && (other: AccessChecker0) = new AccessChecker0 ( (user: User) =>
    this.hasAccess(user) && other.hasAccess(user)
  )
  def unary_! = new AccessChecker0 ( (user: User) =>
    !this.hasAccess(user)
  )

  def -> [V] (value: => V) = new AccessCheckerToLazyVal[V](this, value)
}

/**
 * Checks if user can access to some specified resource of type T.
 * @param predicate (T, User) => Boolean, true if User has access to resource T.
 * @tparam T Type of resource, access to which is checked.
 */
class AccessChecker[-T](predicate: (T, User) => Boolean) {
  
  def hasAccess(entity: T)(implicit user: User): Boolean = predicate(entity, user)

  def apply(entity: T) = new AccessChecker0 ( (user: User) => this.hasAccess(entity)(user))

  def || [O <: T] (other: AccessChecker[O]) = new AccessChecker[O] ( (entity: O, user: User) =>
    this.hasAccess(entity)(user) || other.hasAccess(entity)(user)
  )
  def && [O <: T] (other: AccessChecker[O]) = new AccessChecker[O] ( (entity: O, user: User) =>
    this.hasAccess(entity)(user) && other.hasAccess(entity)(user)
  )
  def unary_! = new AccessChecker[T] ( (entity: T, user: User) =>
    !this.hasAccess(entity)(user)
  )

  def allowIfNone = new AccessChecker[Option[T]] ((optionEntity: Option[T], user: User) =>
    optionEntity match {
      case None => true
      case Some(entity) => hasAccess(entity)(user)
    }
  )

  def denyIfNone = new AccessChecker[Option[T]] ((optionEntity: Option[T], user: User) =>
    optionEntity match {
      case None => false
      case Some(entity) => hasAccess(entity)(user)
    }
  )
}

/**
 * Helper class for accessFold method. Represents tuple (AccessChecker0, Value), the main difference is that value
 * is passed by-name and calculated once only if needed.
 */
class AccessCheckerToLazyVal[+T](val checker: AccessChecker0, aValue: => T) {
  lazy val value: T = aValue
}

object SecurityService {

  /**
   * Implicit conversion to checker of specific resource type. Actual resource object is ignored then.
   */
  implicit def toAnyEntity[T](checker: AccessChecker0): AccessChecker[T] = new AccessChecker[T] (
    (entity: T, user: User) => checker.hasAccess(user)
  )


  /**
   * Wrap method to check if user has access to it and run body if so. Throw Exception otherwise.
   */
  def preAuthorize[V](predicate: AccessChecker0)(value: => V)(implicit user: User): V = {
    if (predicate.hasAccess) value
    else throw AccessDeniedException(s"Access to user ${user} is denied (reason: ${predicate})")
  }

  /**
   * Wrap method to run it and to check if user has access to it's returned object. Throw Exception otherwise.
   */
  def postAuthorize[C >: V, V](predicate: AccessChecker[C])(value: V)(implicit user: User): V = {
    if (predicate.hasAccess(value)) value
    else throw AccessDeniedException(s"Access to object ${value} to user ${user} is denied (reason: ${predicate})")
  }

  /**
   * The same as postAuthorize, but also check if Option is None or Same.
   */
  def postAuthorizeAllowNone[C >: V, V](predicate: AccessChecker[C])(value: Option[V])(implicit user: User) =
    postAuthorize(predicate.allowIfNone)(value)(user)

  /**
   * The same as postAuthorize, but also check if Option is None or Same.
   */
  def postAuthorizeDenyNone[C >: V, V](predicate: AccessChecker[C])(value: Option[V])(implicit user: User) =
    postAuthorize(predicate.denyIfNone)(value)(user)

  /**
   * Wrap method to run it and filter returned sequence so only accessible objects remain.
   */
  def postFilter[C >: V, SV <% Seq[V], V](predicate: AccessChecker[C])(values: SV)(implicit user: User) =
    values.filter(value => predicate.hasAccess(value)(user))

  /**
   * Chose first access rule which returns true and return it's value. Only this value should be calculates (called by name)
   */
  def accessFold[V](cases: AccessCheckerToLazyVal[V]*)(implicit user: User): V = cases find { checkVal: AccessCheckerToLazyVal[V] =>
    checkVal.checker.hasAccess
  } match {
    case None => throw AccessDeniedException("Not authorized to get resource")
    case Some(checkVal) => checkVal.value
  }

  /**
   * Allow all, always return true. Used to determine default behaviour in accessFold method.
   */
  val withDefaultValue = new AccessChecker0(user => true)
}
