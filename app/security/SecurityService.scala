package security

import models.User

import scala.collection.GenTraversable

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
}

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

case class AccessDeniedException(msg: String) extends Exception(msg)

object SecurityService {

  implicit def toAnyEntity[T](checker: AccessChecker0): AccessChecker[T] = new AccessChecker[T] (
    (entity: T, user: User) => checker.hasAccess(user)
  )

//  implicit def toOptionDenyIfNone[T](checker: AccessChecker[T]): AccessChecker[Option[T]] = checker.denyIfNone

  def preAuthorize[V](predicate: AccessChecker0)(value: => V)(implicit user: User): V = {
    if (predicate.hasAccess) value
    else throw new AccessDeniedException(s"Access to user ${user} is denied (reason: ${predicate})")
  }

  def postAuthorize[C >: V, V](predicate: AccessChecker[C])(value: V)(implicit user: User): V = {
    if (predicate.hasAccess(value)) value
    else throw new AccessDeniedException(s"Access to object ${value} to user ${user} is denied (reason: ${predicate})")
  }

  def postAuthorizeAllowNone[C >: V, V](predicate: AccessChecker[C])(value: Option[V])(implicit user: User) =
    postAuthorize(predicate.allowIfNone)(value)(user)

  def postAuthorizeDenyNone[C >: V, V](predicate: AccessChecker[C])(value: Option[V])(implicit user: User) =
    postAuthorize(predicate.denyIfNone)(value)(user)

  def postFilter[C >: V, SV <% Seq[V], V](predicate: AccessChecker[C])(values: SV)(implicit user: User) =
    values.filter(value => predicate.hasAccess(value)(user))
}
