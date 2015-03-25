package security

import exceptions.AccessDeniedException

/**
 * Checks if user can access to some unspecified resource.
 * @param predicate Credentials => Boolean, true if Credentials has access to resource.
 */
class AccessChecker0(predicate: Credentials => Boolean) {

  def hasAccess(implicit credentials: Credentials): Boolean = predicate(credentials)

  def || (other: AccessChecker0) = new AccessChecker0 ( (credentials: Credentials) =>
    this.hasAccess(credentials) || other.hasAccess(credentials)
  )
  def && (other: AccessChecker0) = new AccessChecker0 ( (credentials: Credentials) =>
    this.hasAccess(credentials) && other.hasAccess(credentials)
  )
  def unary_! = new AccessChecker0 ( (credentials: Credentials) =>
    !this.hasAccess(credentials)
  )

  def -> [V] (value: => V) = new AccessCheckerToLazyVal[V](this, value)
}

/**
 * Checks if user can access to some specified resource of type T.
 * @param predicate (T, Credentials) => Boolean, true if Credentials has access to resource T.
 * @tparam T Type of resource, access to which is checked.
 */
class AccessChecker[-T](predicate: (T, Credentials) => Boolean) {
  
  def hasAccess(entity: T)(implicit credentials: Credentials): Boolean = predicate(entity, credentials)

  def apply(entity: T) = new AccessChecker0 ( (credentials: Credentials) => this.hasAccess(entity)(credentials))

  def apply[O](transform: O => T): AccessChecker[O] = new AccessChecker[O] ( (entity: O, credentials: Credentials) =>
    this.hasAccess(transform(entity))(credentials)
  )

  def || [O <: T] (other: AccessChecker[O]) = new AccessChecker[O] ( (entity: O, credentials: Credentials) =>
    this.hasAccess(entity)(credentials) || other.hasAccess(entity)(credentials)
  )
  def && [O <: T] (other: AccessChecker[O]) = new AccessChecker[O] ( (entity: O, credentials: Credentials) =>
    this.hasAccess(entity)(credentials) && other.hasAccess(entity)(credentials)
  )
  def unary_! = new AccessChecker[T] ( (entity: T, credentials: Credentials) =>
    !this.hasAccess(entity)(credentials)
  )

  def allowIfNone = new AccessChecker[Option[T]] ((optionEntity: Option[T], credentials: Credentials) =>
    optionEntity match {
      case None => true
      case Some(entity) => hasAccess(entity)(credentials)
    }
  )

  def denyIfNone = new AccessChecker[Option[T]] ((optionEntity: Option[T], credentials: Credentials) =>
    optionEntity match {
      case None => false
      case Some(entity) => hasAccess(entity)(credentials)
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
    (entity: T, credentials: Credentials) => checker.hasAccess(credentials)
  )

  /**
   * Implicit conversion to predicate (T => Boolean)
   */
  implicit def toPredicate[T](checker: AccessChecker[T])(implicit credentials: Credentials): (T => Boolean) =
    (entity: T) => checker.hasAccess(entity)(credentials)

  /**
   * Wrap method to check if Credentials has access to it and run body if so. Throw Exception otherwise.
   */
  def preAuthorize[V](predicate: AccessChecker0)(value: => V)(implicit credentials: Credentials): V = {
    if (predicate.hasAccess) value
    else throw AccessDeniedException(s"Access to Credentials ${credentials} is denied (reason: ${predicate})")
  }

  /**
   * Wrap method to run it and to check if Credentials has access to it's returned object. Throw Exception otherwise.
   */
  def postAuthorize[C >: V, V](predicate: AccessChecker[C])(value: V)(implicit credentials: Credentials): V = {
    if (predicate.hasAccess(value)) value
    else throw AccessDeniedException(s"Access to object ${value} to Credentials ${credentials} is denied (reason: ${predicate})")
  }

  /**
   * The same as postAuthorize, but also check if Option is None or Same.
   */
  def postAuthorizeAllowNone[C >: V, V](predicate: AccessChecker[C])(value: Option[V])(implicit credentials: Credentials) =
    postAuthorize(predicate.allowIfNone)(value)(credentials)

  /**
   * The same as postAuthorize, but also check if Option is None or Same.
   */
  def postAuthorizeDenyNone[C >: V, V](predicate: AccessChecker[C])(value: Option[V])(implicit credentials: Credentials) =
    postAuthorize(predicate.denyIfNone)(value)(credentials)

  /**
   * Wrap method to run it and filter returned sequence so only accessible objects remain.
   */
  def postFilter[C >: V, SV <% Seq[V], V](predicate: AccessChecker[C])(values: SV)(implicit credentials: Credentials) =
    values.filter(value => predicate.hasAccess(value)(credentials))

  /**
   * Chose first access rule which returns true and return it's value. Only this value should be calculates (called by name)
   */
  def accessFold[V](cases: AccessCheckerToLazyVal[V]*)(implicit credentials: Credentials): V = cases find { checkVal: AccessCheckerToLazyVal[V] =>
    checkVal.checker.hasAccess
  } match {
    case None => throw AccessDeniedException("Not authorized to get resource")
    case Some(checkVal) => checkVal.value
  }

  /**
   * Allow all, always return true. Used to determine default behaviour in accessFold method.
   */
  val withDefaultValue = new AccessChecker0(Credentials => true)
}
