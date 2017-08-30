package com.sharkdtu.mlwheel

import java.io.File

import org.scalatest.{BeforeAndAfterAll, FunSuite, Outcome}


abstract class MLWheelFunSuite extends FunSuite with BeforeAndAfterAll with Logging {

  protected override def beforeAll(): Unit = {
    super.beforeAll()
    // Do something before test
  }

  protected override def afterAll(): Unit = {
    // Do something after test
    super.afterAll()
  }

  /**
   * Log the suite name and the test name before and after each test.
   *
   * Subclasses should never override this method. If they wish to run
   * custom code before and after each test, they should mix in the
   * {{org.scalatest.BeforeAndAfter}} trait instead.
   */
  final protected override def withFixture(test: NoArgTest): Outcome = {
    val testName = test.text
    val suiteName = this.getClass.getName
    try {
      logInfo(s"\n===== TEST OUTPUT FOR $suiteName: '$testName' =====\n")
      test()
    } finally {
      logInfo(s"\n===== FINISHED $suiteName: '$testName' =====\n")
    }
  }

  // helper function
  protected final def getTestResourceFile(file: String): File = {
    new File(getClass.getClassLoader.getResource(file).getFile)
  }

  protected final def getTestResourcePath(file: String): String = {
    getTestResourceFile(file).getCanonicalPath
  }
}
