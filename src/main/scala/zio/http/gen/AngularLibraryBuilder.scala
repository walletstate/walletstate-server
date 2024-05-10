package zio.http.gen

import zio.*
import zio.http.gen.ts.{TSHttpService, TSType}
import zio.process.Command

import java.io.{File, PrintWriter}
import scala.io.Source

object AngularLibraryBuilder {
  private val AngularWorkDirName     = "angular-workspace"
  private val AngularLibraryScope    = "walletstate"
  private val AngularLibraryName     = "angular-client"
  private val AngularLibraryFullName = s"@$AngularLibraryScope/$AngularLibraryName"

  def build(services: Chunk[TSHttpService], version: String): Task[Unit] = for {
    _           <- Console.printLine("Creating Angular Project")
    workspace   <- initAngularWorkspace(new File("target"))
    libDir      <- initLibrary(workspace)
    _           <- cleanupGeneratedExamples(libDir)
    _           <- updatePackageJson(libDir, version)
    _           <- Console.printLine("Angular project initiated. Generating source code ...")
    customTypes <- writeCustomTypes(libDir, services)
    _           <- writeServices(libDir, services)
    _           <- writePublicApi(libDir, customTypes, services)
    _           <- Console.printLine(s"Source code generated. Building and  publishing version $version ...")
    _           <- buildLibrary(workspace)
    _           <- publishLibrary(workspace)
    _           <- Console.printLine("Done")
  } yield ()

  private def initAngularWorkspace(dir: File) = for {
    r <- Command("ng", "new", AngularWorkDirName, "--no-create-application").workingDirectory(dir).string
    _ <- Console.printLine(r)
  } yield dir.toPath.resolve(AngularWorkDirName).toFile

  private def initLibrary(workspace: File) = for {
    r <- Command("ng", "generate", "library", AngularLibraryFullName).workingDirectory(workspace).string
    _ <- Console.printLine(r)
  } yield workspace.toPath.resolve(s"projects/$AngularLibraryScope/$AngularLibraryName").toFile

  private def cleanupGeneratedExamples(libDir: File) = for {
    _ <- ZIO.attempt(libDir.toPath.resolve("src/lib").toFile.listFiles().foreach(_.delete()))
    _ <- ZIO.attempt(libDir.toPath.resolve("src/public-api.ts").toFile.delete())
  } yield ()

  private def buildLibrary(workspace: File) = for {
    r <- Command("ng", "build", AngularLibraryFullName).workingDirectory(workspace).string
    _ <- Console.printLine(r)
  } yield ()

  private def updatePackageJson(libDir: File, version: String) = for {
    packageJsonFile <- ZIO.succeed(libDir.toPath.resolve("package.json").toFile)
    content         <- readFile(packageJsonFile)
    updatedContent = content.replace(
      s"  \"version\": \"0.0.1\",",
      s"  \"version\": \"${version}\",\n  \"repository\": \"https://github.com/walletstate/walletstate-server\","
    )
    _ <- ZIO.attempt(packageJsonFile.delete())
    _ <- writeFile(packageJsonFile, updatedContent)
  } yield ()

  private def publishLibrary(workspace: File) = for {
    r <- Command("npm", "publish", s"--@$AngularLibraryScope:registry=https://npm.pkg.github.com")
      .workingDirectory(workspace.toPath.resolve(s"dist/$AngularLibraryScope/$AngularLibraryName").toFile)
      .string
    _ <- Console.printLine(r)
  } yield ()

  private def writeCustomTypes(libDir: File, services: Chunk[TSHttpService]): Task[Chunk[TSType.TSCustomType]] = {
    val writeTo     = libDir.toPath.resolve("src/lib/models")
    val customTypes = services.flatMap(_.allCustomTypes).distinctBy(_.`type`)

    for {
      _ <- if (!writeTo.toFile.exists()) ZIO.attempt(writeTo.toFile.mkdir()) else ZIO.unit
      _ <- customTypes.mapZIO(ct => writeFile(writeTo.resolve(ct.fileName).toFile, ct.renderContent))
    } yield customTypes
  }

  private def writeServices(libDir: File, services: Chunk[TSHttpService]) = {
    val writeTo = libDir.toPath.resolve("src/lib")
    services.mapZIO(service => writeFile(writeTo.resolve(service.fileName).toFile, service.render))
  }

  private def writePublicApi(libDir: File, customTypes: Chunk[TSType.TSCustomType], services: Chunk[TSHttpService]) = {
    val customTypesExports   = customTypes.map(_.exportFrom("./lib/models"))
    val servicesExports      = services.map(_.exportFrom("./lib"))
    val publicApiFileContent = (customTypesExports ++ servicesExports).mkString("\n")

    writeFile(libDir.toPath.resolve("src/public-api.ts").toFile, publicApiFileContent)
  }

  private def writeFile(dest: File, content: String) = {
    val writer = ZIO.acquireRelease(ZIO.succeed(new PrintWriter(dest)))(pw => ZIO.succeed(pw.close()))
    ZIO.scoped { writer.map(w => w.write(content)) }
  }

  private def readFile(file: File) = {
    val fileSource = ZIO.acquireRelease(ZIO.succeed(Source.fromFile(file)))(fs => ZIO.succeed(fs.close()))
    ZIO.scoped { fileSource.map(_.getLines().mkString("\n")) }
  }
}
