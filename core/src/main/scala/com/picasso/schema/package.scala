package com.picasso

import com.picasso.model.Platform
import dynosaur.Schema

import java.time.Instant

package object schema {
  lazy val schemaInstant: Schema[Instant] = Schema[String].imap(s => Instant.parse(s))(_.toString)

  lazy val platformSchema: Schema[Platform] = Schema[String].imapErr { s =>
    Platform.parse(s).toRight(Schema.ReadError(s"$s is not valid Platform"))
  }(_.toString)
}
