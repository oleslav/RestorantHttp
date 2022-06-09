CREATE TABLE todo (
  id SERIAL PRIMARY KEY,
  description TEXT,
  importance TEXT
);

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name TEXT,
  surname TEXT
);

CREATE TABLE feedbacks (
  id SERIAL PRIMARY KEY NOT NULL,
  description TEXT,
  rating int8 NOT NULL,
  restaurant_id int8 NOT NULL,
  user_id int8 NOT NULL
);

CREATE TABLE restaurants (
  id SERIAL PRIMARY KEY,
  name TEXT,
  rating DOUBLE
);

ALTER TABLE feedbacks
ADD CONSTRAINT BOOKS_AUTHOR_ID_FK
FOREIGN KEY (restaurant_id) REFERENCES restaurants;

ALTER TABLE feedbacks
ADD CONSTRAINT BOOKS_USER_ID_FK
FOREIGN KEY (user_id) REFERENCES users;