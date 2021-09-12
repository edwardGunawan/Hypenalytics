package com.picasso.util

trait Codec {

  def encodeBase64(str:String): String = new String(java.util.Base64.getEncoder.encode(str.getBytes()))

  def decodeBase64(token:String): String = new String(java.util.Base64.getDecoder.decode(token))

}
