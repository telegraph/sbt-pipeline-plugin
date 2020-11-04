package uk.co.telegraph.cloud

sealed trait TemplateType {
  def format:String
}

object JsonFormat extends TemplateType{
  val format:String = "json"
}

object YamlFormat extends TemplateType{
  val format:String = "yaml"
}

object YmlFormat  extends TemplateType{
  val format:String = "yml"
}