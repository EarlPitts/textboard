import cats.effect.*
import cats.effect.std.*
import cats.implicits.*
import textboard.domain.*
import textboard.services.*

import weaver.*

object PostsSuite extends SimpleIOSuite:
  val testThread = InMemThread(1, "Test Thread", "Test text", List.empty)

  test("Create post increments counter and adds post") {
    val postText = "Hello, world!"
    for
      counter <- Ref.of[IO, Int](0)
      service = InMemPosts.inMemPosts[IO](counter)
      post <- service.create(postText)
      count <- counter.get
    yield expect(count == 1) and expect(post.text == postText)
  }

  test("Get post by id returns the correct post") {
    val postText = "Hello, world!"
    for
      counter <- Ref.of[IO, Int](0)
      service = InMemPosts.inMemPosts[IO](counter)
      post <- service.create(postText)
      retrievedPost <- service.get(post.id, testThread.copy(posts = List(post)))
    yield expect(retrievedPost.contains(post))
  }

  test("Get all posts returns all posts in the thread") {
    val postText1 = "Hello, world!"
    val postText2 = "Another post"
    for
      counter <- Ref.of[IO, Int](0)
      service = InMemPosts.inMemPosts[IO](counter)
      post1 <- service.create(postText1)
      post2 <- service.create(postText2)
      allPosts <- service.getAll(testThread.copy(posts = List(post1, post2)))
    yield expect(allPosts == List(post1, post2))
  }

  test("Create post is thread-safe") {
    val postText = "Hello, world!"
    val postNum = 500
    for
      counter <- Ref.of[IO, Int](0)
      service = InMemPosts.inMemPosts[IO](counter)
      posts <- service.create(postText).parReplicateA(postNum)
    yield expect(posts.map(_.id).distinct.size === postNum)
  }
