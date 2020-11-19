package org.cafienne.cmmn.instance.casefile.document

import java.io.File

import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.server.Directives.{getFromFile, onComplete}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.FileIO
import com.typesafe.scalalogging.LazyLogging
import org.cafienne.akka.actor.CaseSystem
import org.cafienne.akka.actor.config.ConfigurationException
import org.cafienne.akka.actor.identity.TenantUser
import org.cafienne.cmmn.instance.casefile.Path

import scala.concurrent.Future

class FileBasedDocumentStorage extends DocumentStorage with LazyLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val materializer = CaseSystem.system

  val storageDirectory: File = {
    val location = CaseSystem.config.documentStorage.location
    val file = new File(location)
    if (!file.exists) {
      throw ConfigurationException(s"Storage location '$location' does not exist")
    }
    if (!file.isDirectory) {
      throw ConfigurationException(s"Storage location '$location' must be a directory")
    }
    logger.info(s"Storage location for uploading & downloading documents ${file.getAbsolutePath}")
    file
  }

  override def upload(user: TenantUser, caseInstanceId: String, path: Path, bodyPart: FormData.BodyPart): Future[DocumentIdentifier] = {

    // stream into a file as the chunks of it arrives and return a future
    // file to where it got stored
    val newFileName = bodyPart.filename.getOrElse({
      println("GENERATING NEW NAME.TXT")
      "new_name.txt"
    })

    val file: File = new File(getDirectory(caseInstanceId, path, true), newFileName)
    bodyPart.entity.dataBytes.runWith(FileIO.toPath(file.toPath)).map(_ => bodyPart.name -> file).map(result => {
      logger.info("Uploaded " + file.getAbsolutePath)
      DocumentIdentifier(newFileName)
    })
  }

  private def getDirectory(caseInstanceId: String, path: Path, createIfNotExists: Boolean = false): File = {
    val caseInstancePath: File = new File(storageDirectory, caseInstanceId)
    if (createIfNotExists && !caseInstancePath.exists()) {
      caseInstancePath.mkdir()
    }
    val caseFileItemPath = new File(caseInstancePath, path.toString)
    if (createIfNotExists && !caseFileItemPath.exists()) {
      caseFileItemPath.mkdirs()
    }
    caseFileItemPath
  }

  override def download(user: TenantUser, caseInstanceId: String, path: Path): Route = {
    val directory = getDirectory(caseInstanceId, path)
    getFromFile(directory)
  }

  override def removeUpload(user: TenantUser, caseInstanceId: String, path: Path): Future[StorageResult] = {
    ???
  }
}
