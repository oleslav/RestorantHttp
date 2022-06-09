package service

import cats.effect.IO
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import model.{User, FeedBack, Restaurant, UserNotFoundError, RestaurantNotFoundError, FeedBackNotFoundError}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import repository.LibraryRepository

class LibraryService(repository: LibraryRepository) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "users" =>github
      Ok(Stream("[") ++ repository.getUsers.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))

    case GET -> Root / "feedbacks" =>
      Ok(Stream("[") ++ repository.getFeedbacks.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))

    case GET -> Root / "restaurants" =>
      Ok(Stream("[") ++ repository.getRestaurants.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))

    case GET -> Root / "good_restaurants" =>
      Ok(Stream("[") ++ repository.getGoodRestaurants.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))

    case GET -> Root / "bad_restaurants" =>
      Ok(Stream("[") ++ repository.getBadRestaurants.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))

    case GET -> Root / "users" / LongVar(id) =>
      for {
        getResult <- repository.getUser(id)
        response <- userResult(getResult)
      } yield response

    case GET -> Root / "feedbacks" / LongVar(id) =>
      for {
        getResult <- repository.getFeedback(id)
        response <- feedbackResult(getResult)
      } yield response

    case GET -> Root / "restaurants" / LongVar(id) =>
      for {
        getResult <- repository.getRestaurant(id)
        response <- restaurantResult(getResult)
      } yield response

    case req@POST -> Root / "users" =>
      for {
        user <- req.decodeJson[User]
        createdUser <- repository.createUser(user)
        response <- Created(createdUser.asJson, Location(Uri.unsafeFromString(s"/users/${createdUser.id.get}")))
      } yield response

    case req@POST -> Root / "restaurants" =>
      for {
        restaurant <- req.decodeJson[Restaurant]
        createdRestaurant <- repository.createRestaurant(restaurant)
        response <- Created(createdRestaurant.asJson, Location(Uri.unsafeFromString(s"/restaurants/${createdRestaurant.id.get}")))
      } yield response

    case req@POST -> Root / "feedbacks" =>
      for {
        feedback <- req.decodeJson[FeedBack]
        createdFeedback <- repository.createFeedback(feedback)
        response <- Created(createdFeedback.asJson, Location(Uri.unsafeFromString(s"/feedbacks/${createdFeedback.id.get}")))
      } yield response

    case req@PUT -> Root / "users" / LongVar(id) =>
      for {
        user <- req.decodeJson[User]
        updateResult <- repository.updateUser(id, user)
        response <- userResult(updateResult)
      } yield response

    case req@PUT -> Root / "restaurants" / LongVar(id) =>
      for {
        restaurant <- req.decodeJson[Restaurant]
        updateResult <- repository.updateRestaurant(id, restaurant)
        response <- restaurantResult(updateResult)
      } yield response

    case req@PUT -> Root / "feedbacks" / LongVar(id) =>
      for {
        feedback <- req.decodeJson[FeedBack]
        updateResult <- repository.updateFeedback(id, feedback)
        response <- feedbackResult(updateResult)
      } yield response

    case DELETE -> Root / "users" / LongVar(id) =>
      repository.deleteUser(id).flatMap {
        case Left(UserNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }

    case DELETE -> Root / "restaurants" / LongVar(id) =>
      repository.deleteRestaurant(id).flatMap {
        case Left(RestaurantNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }

    case DELETE -> Root / "feedbacks" / LongVar(id) =>
      repository.deleteFeedback(id).flatMap {
        case Left(FeedBackNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }
  }

  private def userResult(result: Either[UserNotFoundError.type, User]) = {
    result match {
      case Left(UserNotFoundError) => NotFound()
      case Right(user) => Ok(user.asJson)
    }
  }

  private def restaurantResult(result: Either[RestaurantNotFoundError.type, Restaurant]) = {
    result match {
      case Left(RestaurantNotFoundError) => NotFound()
      case Right(restaurant) => Ok(restaurant.asJson)
    }
  }

  private def feedbackResult(result: Either[FeedBackNotFoundError.type, FeedBack]) = {
    result match {
      case Left(FeedBackNotFoundError) => NotFound()
      case Right(feedback) => Ok(feedback.asJson)
    }
  }
}
