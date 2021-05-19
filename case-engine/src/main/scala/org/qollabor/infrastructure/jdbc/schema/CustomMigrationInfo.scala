package org.qollabor.infrastructure.jdbc.schema

import slick.migration.api.Migration
import slick.migration.api.flyway.MigrationInfo
import slick.migration.api.flyway.MigrationInfo.Provider

/**
  * Due to an earlier bug in slick flyway migration library, description did not give repeated predictable outcome.
  * Therefore in Qollabor we made a CustomMigrationInfo (called MigrationInfoHack) to overcome this problem.
  *
  * Original ticket: https://github.com/nafg/slick-migration-api-flyway/issues/26
  *
  * The bug has been fixed in the library; however, in the new version, the construction of the description is done
  * in a different manner than in the Qollabor version. Hence we need to continue to use our own version.
  * So we have renamed it to CustomMigrationInfo instead of MigrationInfoHack...
  */
object CustomMigrationInfo {
  import slick.migration.api.flyway.MigrationInfo.Provider.{crc32, sql}

  def provider: Provider[Migration] = {
    new Provider[Migration]({ migration =>
      val sqlStrings = sql(migration)

      MigrationInfo(
        description = migration.getClass.getSimpleName, // <- actual override
        script = sqlStrings.mkString("\n"),
        checksum = Some(crc32(sqlStrings).toInt),
        location = migration.getClass.getName
      )
    })
  }
}
