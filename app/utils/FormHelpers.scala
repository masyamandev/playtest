package utils

import play.api.data.validation.{Invalid, Valid, Constraint}

object FormHelpers {
  def toConstraint[T](predicate: T => Boolean, errorMessage: => String) =
    Constraint[T] { value: T =>
      if (!predicate(value)) Valid
      else Invalid(errorMessage)
    }
}
