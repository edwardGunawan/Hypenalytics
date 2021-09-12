package com.picasso.util

import cats.effect.Async

import java.util.concurrent.{CompletableFuture, CompletionException}

trait IOUtil {
  implicit class AsyncOps[F[_]](as: Async[F]) {

    def fromJavaFuture[A](fut: => CompletableFuture[A]): F[A] = as.async_[A] { cb =>
      fut.handle[Unit] { (a, x) =>
        if (a == null) {
          x match {
            case t: CompletionException => cb(Left(t.getCause))
            case t => cb(Left(t))
          }
        } else {
          cb(Right(a))
        }
      }
      ()
    }
  }

}
