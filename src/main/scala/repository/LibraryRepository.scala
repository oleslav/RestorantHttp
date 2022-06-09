package repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import model.{User, FeedBack, Restaurant, UserNotFoundError, RestaurantNotFoundError, FeedBackNotFoundError}
import doobie._
import doobie.implicits._

class LibraryRepository(transactor: Transactor[IO]) {
  def getUsers: Stream[IO, User] = {
    sql"SELECT * FROM users".query[User].stream.transact(transactor)
  }

  def getRestaurants: Stream[IO, Restaurant] = {
    sql"SELECT * FROM restaurants".query[Restaurant].stream.transact(transactor)
  }

  def getGoodRestaurants: Stream[IO, Restaurant] = {
    sql"SELECT * FROM restaurants".query[Restaurant].stream.filter(restaurant => restaurant.rating >= 3).transact(transactor)
  }

  def getBadRestaurants: Stream[IO, Restaurant] = {
    sql"SELECT * FROM restaurants".query[Restaurant].stream.filter(restaurant => restaurant.rating < 3).transact(transactor)
  }

  def getFeedbacks: Stream[IO, FeedBack] = {
    sql"SELECT * FROM feedbacks".query[FeedBack].stream.transact(transactor)
  }

  def getUser(id: Long): IO[Either[UserNotFoundError.type, User]] = {
    sql"SELECT id, name, surname FROM users WHERE id = $id".query[User].option.transact(transactor).map {
      case Some(user) => Right(user)
      case None => Left(UserNotFoundError)
    }
  }

  def getFeedback(id: Long): IO[Either[FeedBackNotFoundError.type, FeedBack]] = {
    sql"SELECT * FROM feedbacks WHERE id = $id".query[FeedBack].option.transact(transactor).map {
      case Some(feedback) => Right(feedback)
      case None => Left(FeedBackNotFoundError)
    }
  }

  def getFeedbackbyRestorauntId(restaurant_id: Long): IO[Either[FeedBackNotFoundError.type, FeedBack]] = {
    sql"SELECT * FROM feedbacks WHERE restaurant_id = $restaurant_id".query[FeedBack].option.transact(transactor).map {
      case Some(feedback) => Right(feedback)
      case None => Left(FeedBackNotFoundError)
    }
  }

  def getRestaurant(id: Long): IO[Either[RestaurantNotFoundError.type, Restaurant]] = {
    sql"SELECT id, name, rating FROM restaurants WHERE id = $id".query[Restaurant].option.transact(transactor).map {
      case Some(restaurant) => Right(restaurant)
      case None => Left(RestaurantNotFoundError)
    }
  }

  def createUser(user: User): IO[User] = {
    sql"INSERT INTO users (name, surname) VALUES (${user.name}, ${user.surname})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      user.copy(id = Some(id))
    }
  }

  def createRestaurant(restaurant: Restaurant): IO[Restaurant] = {
    sql"INSERT INTO restaurants (name, rating) VALUES (${restaurant.name}, ${restaurant.rating})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      restaurant.copy(id = Some(id))
    }
  }

  def createFeedback(feedback: FeedBack): IO[FeedBack] = {
    sql"INSERT INTO feedbacks (description, rating, restaurant_id, user_id) VALUES (${feedback.description}, ${feedback.rating}, ${feedback.restaurant_id}, ${feedback.user_id})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      feedback.copy(id = Some(id))
    }
  }

  def deleteUser(id: Long): IO[Either[UserNotFoundError.type, Unit]] = {
    sql"DELETE FROM users WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(UserNotFoundError)
      }
    }
  }

  def deleteRestaurant(id: Long): IO[Either[RestaurantNotFoundError.type, Unit]] = {
    sql"DELETE FROM restaurants WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(RestaurantNotFoundError)
      }
    }
  }

  def deleteFeedback(id: Long): IO[Either[FeedBackNotFoundError.type, Unit]] = {
    sql"DELETE FROM feedbacks WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(FeedBackNotFoundError)
      }
    }
  }

  def updateUser(id: Long, user: User): IO[Either[UserNotFoundError.type, User]] = {
    sql"UPDATE users SET name = ${user.name}, surname = ${user.surname} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(user.copy(id = Some(id)))
      } else {
        Left(UserNotFoundError)
      }
    }
  }

  def updateRestaurant(id: Long, restaurant: Restaurant): IO[Either[RestaurantNotFoundError.type, Restaurant]] = {
    sql"UPDATE restaurants SET name = ${restaurant.name}, rating = ${restaurant.rating} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(restaurant.copy(id = Some(id)))
      } else {
        Left(RestaurantNotFoundError)
      }
    }
  }

  def updateFeedback(id: Long, feedback: FeedBack): IO[Either[FeedBackNotFoundError.type, FeedBack]] = {
    sql"UPDATE feedbacks SET name = ${feedback.description}, restaurant_id = ${feedback.restaurant_id}, user_id = ${feedback.user_id} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(feedback.copy(id = Some(id)))
      } else {
        Left(FeedBackNotFoundError)
      }
    }
  }

}
