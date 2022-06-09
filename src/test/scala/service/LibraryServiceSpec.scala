package service

import cats.effect.IO
import fs2.Stream
import io.circe.Json
import io.circe.literal._
import model.{User, FeedBack, Restaurant, UserNotFoundError, RestaurantNotFoundError, FeedBackNotFoundError}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s._
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repository.LibraryRepository

class LibraryServiceSpec extends AnyWordSpec with MockFactory with Matchers {
  private val repository = stub[LibraryRepository]

  private val service = new LibraryService(repository).routes

  "LibraryService" should {

    // User part
    "create a user" in {
      val id = 1
      val user = User(None, "name", "surname")
      (repository.createUser _).when(user).returns(IO.pure(user.copy(id = Some(id))))
      val createJson = json"""
        {
          "name": ${user.name},
          "surname": ${user.surname}
        }"""

      val response = serve(Request[IO](POST, uri"/users").withEntity(createJson))
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${user.name},
          "surname": ${user.surname}
        }"""
    }

    "update a user" in {
      val id = 1
      val user = User(None, "name", "surname")
      (repository.updateUser _).when(id, user).returns(IO.pure(Right(user.copy(id = Some(id)))))
      val updateJson = json"""
        {
          "name": ${user.name},
          "surname": ${user.surname}
        }"""

      val response = serve(Request[IO](PUT, Uri.unsafeFromString(s"/users/$id")).withEntity(updateJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${user.name},
          "surname": ${user.surname}
        }"""
    }


    "return a single user" in {
      val id = 1
      val user = User(Some(id), "name", "surname")
      (repository.getUser _).when(id).returns(IO.pure(Right(user)))

      val response = serve(Request[IO](GET, Uri.unsafeFromString(s"/users/$id")))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${user.name},
          "surname": ${user.surname}
        }"""
    }

    "return all users" in {
      val id1 = 1
      val user1 = User(Some(id1), "name", "surname")
      val id2 = 2
      val user2 = User(Some(id2), "name", "surname")
      val users = Stream(user1, user2)
      (() => repository.getUsers ).when().returns(users)

      val response = serve(Request[IO](GET, uri"/users"))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        [
         {
            "id": $id1,
            "name": ${user1.name},
            "surname": ${user1.surname}
         },
         {
            "id": $id2,
            "name": ${user2.name},
            "surname": ${user2.surname}
         }
        ]"""
    }

    "delete a user" in {
      val id = 1
      (repository.deleteUser _).when(id).returns(IO.pure(Right(())))

      val response = serve(Request[IO](DELETE, Uri.unsafeFromString(s"/users/$id")))
      response.status shouldBe Status.NoContent
    }

    // Author part
    "create a restaurant" in {
      val id = 1
      val restaurant = Restaurant(None, "Gustav++", 4)
      (repository.createRestaurant _).when(restaurant).returns(IO.pure(restaurant.copy(id = Some(id))))
      val createJson = json"""
        {
          "name": ${restaurant.name},
          "rating": ${restaurant.rating}
        }"""

      val response = serve(Request[IO](POST, uri"/restaurants").withEntity(createJson))
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${restaurant.name},
          "rating": ${restaurant.rating}
        }"""
    }

    "update a restaurant" in {
      val id = 1
      val restaurant = Restaurant(None, "Puzata Hata", 3)
      (repository.updateRestaurant _).when(id, restaurant).returns(IO.pure(Right(restaurant.copy(id = Some(id)))))
      val updateJson = json"""
        {
          "name": ${restaurant.name},
          "rating": ${restaurant.rating}
        }"""

      val response = serve(Request[IO](PUT, Uri.unsafeFromString(s"/restaurants/$id")).withEntity(updateJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${restaurant.name},
          "rating": ${restaurant.rating}
        }"""
    }


    "return a single restaurant" in {
      val id = 1
      val author = Restaurant(Some(id), "Puzata Hatav2", 2)
      (repository.getRestaurant _).when(id).returns(IO.pure(Right(author)))

      val response = serve(Request[IO](GET, Uri.unsafeFromString(s"/restaurants/$id")))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${author.name},
          "rating": ${author.rating}
        }"""
    }

    "return all authors" in {
      val id1 = 1
      val restaurant1 = Restaurant(Some(id1), "Puzata Hata", 3)
      val id2 = 2
      val restaurant2 = Restaurant(Some(id2), "Puzata Hatav2", 2)
      val restaurants = Stream(restaurant1, restaurant2)
      (() => repository.getRestaurants ).when().returns(restaurants)

      val response = serve(Request[IO](GET, uri"/restaurants"))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        [
         {
            "id": $id1,
            "name": ${restaurant1.name},
            "rating": ${restaurant1.rating}
         },
         {
            "id": $id2,
            "name": ${restaurant2.name},
            "rating": ${restaurant2.rating}
         }
        ]"""
    }

    "delete a restaurant" in {
      val id = 1
      (repository.deleteRestaurant _).when(id).returns(IO.pure(Right(())))

      val response = serve(Request[IO](DELETE, Uri.unsafeFromString(s"/restaurants/$id")))
      response.status shouldBe Status.NoContent
    }

    // Book part
    "create a feedback" in {
      val id = 1
      val feedBack = FeedBack(None, "От параша більше їсти тут не буду", 0, 1, 1)
      (repository.createFeedback _).when(feedBack).returns(IO.pure(feedBack.copy(id = Some(id))))
      val createJson = json"""
        {
          "description": ${feedBack.description},
          "rating": ${feedBack.rating},
          "restaurant_id": ${feedBack.restaurant_id},
          "user_id": ${feedBack.user_id}
        }"""

      val response = serve(Request[IO](POST, uri"/feedbacks").withEntity(createJson))
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": ${feedBack.description},
          "rating": ${feedBack.rating},
          "restaurant_id": ${feedBack.restaurant_id},
          "user_id": ${feedBack.user_id}
        }"""
    }

  }

  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
