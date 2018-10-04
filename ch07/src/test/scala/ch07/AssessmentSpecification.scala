package ch07

import org.scalacheck._

object AssessmentSpecification extends Properties("Assessment") {

 import MonoidSpecification._

  property("boolean under or") = {
    import Assessment.booleanOr
    monoidProp[Boolean]
  }

  property("boolean under and") = {
    import Assessment.booleanAnd
    monoidProp[Boolean]
  }

  property("Option[Int] under addition") = {
    import Monoid.intAddition
    import Assessment.option
    monoidProp[Option[Int]]
  }

  property("Option[String] under concatenation") = {
    import Monoid.stringConcatenation
    import Assessment.option
    monoidProp[Option[String]]
  }

  property("Either[Int] under multiplication") = {
    import Monoid.intMultiplication
    implicit val monoid: Monoid[Either[Unit, Int]] = Assessment.either[Unit, Int]
    monoidProp[Either[Unit, Int]]
  }

  property("Either[Boolean] under OR") = {
    import Assessment.booleanOr
    implicit val monoid: Monoid[Either[String, Boolean]] = Assessment.either[String, Boolean]
    monoidProp[Either[String, Boolean]]
  }

}
