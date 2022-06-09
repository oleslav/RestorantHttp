package object model {

  case class User(id: Option[Long], name: String, surname: String)

  case class Restaurant(id: Option[Long], name: String, rating: Double)

  case class FeedBack(id: Option[Long], description: String, rating: Int, restaurant_id: Long, user_id: Long)

  case object UserNotFoundError

  case object RestaurantNotFoundError

  case object FeedBackNotFoundError
}
