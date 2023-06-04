ThisProject / scalaVersion := "3.2.1"

ThisProject / name         := "walletstate"
ThisProject / organization := "online.walletstate"
ThisProject / mainClass    := Some("online.walletstate.Application")

ThisProject / libraryDependencies ++= Dependencies.all
ThisProject / libraryDependencies ++= Dependencies.tests

ThisProject / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
Test / fork := true //is needed for testcontainers
