# Fulton Android
A simple and easy to use Android library for handling REST API or RESTFul API. It is part of the [Fulton](https://github.com/swarmnyc/fulton) family.

# Installation For Gradle
## Step 1 - Add it in your root build.gradle at the end of repositories:

``` gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```


## Step 2 - Add the dependency:
[![Release](https://jitpack.io/v/swarmnyc/fulton-android.svg)](https://jitpack.io/#swarmnyc/fulton-android) <-- Latest Version
``` gradle
dependencies {
    implementation 'com.github.swarmnyc:fulton-android:$version'
}
```

# How To

## Get Started
Many operations for Android need android.context.Context. Therefore, Fulton-Android needs to be initialized before it can do anything.

``` kotlin
// it can be initialized on create of an application
class MyApplication : Application() {
    override fun onCreate() {
        Fulton.init(this)
    }
}

// or on create of an activity
class MainActivity : Activity() {
    override fun onCreate() {
        Fulton.init(this)
    }
}
```

Fulton-Android uses Promise-Like to handle its async http request. It is very similar to Promise of JavaScript which is very easy to use. For Example,

``` kotlin
FooApiClient().barList().then { list ->
    // convert List<Bar> to List<BarViewModel>
    list.map { BarViewModel(it) }
}.thenUi { vmList ->
    // update UI
}.catchUi { error ->
    // show error
}
```

Each Api Call returns a Promise object. From there, you can use `.then` to do stuff that doesn't require the UI thread or use `.thenUi` to update UI. Also, use `.catch` or `.catchUi` to handle errors. 

There are many other features. See [PromiseKt](https://github.com/swarmnyc/PromiseKt) for more information.

## ApiClient
ApiClient is an abstract class that handles REST APIs. You can use one ApiClient to handle any requests, but we recommend creating different ApiClients for different API routes. For Example,

``` kotlin
/* ProductApiClient handles all API calls of Product */
class ProductApiClient : ApiClient() {
    override val urlRoot = "https://api.your-domain.com/products"

    fun search(keyword:String, limit:Int) : Promise<List<Product>> {
        return request {
            // the request becomes GET https://api.your-domain.com/products/search?keyword=${keyword}&limit=${limit}
            paths("latest")
            query("keyword" to keyword, "limit" to limit) 
            
            // Java has problems for Generic, so if the return Type has generics
            // The generics have be manually defined.
            // Like there, List<Product>, News have to be defined.
            resultTypeGenerics(Product::class.java) 
        }
    }

    fun create(product:Product) : Promise<Product> {
        return request {
            // the request becomes POST https://api.your-domain.com/products
            method = Method.Post
            body(product)
        }
    }
}
```

The following two functions are the basic functions to make HTTP calls.
- `fun <T> request(init: Request.() -> Unit): Promise<T>`, The parameter is a block that you can initialize the Request object inside of. 
- `fun <T> request(req: Request): Promise<T>`, The parameter is a Request object. 

We recommend that you use the first function because it is easier to use. For example,

``` kotlin
fun foo() : Promise<Bar> {
    return request { // "this" is a Request object
        // initialize the Request object inside the lambda function, like
        paths("bar")
    }
}

// this serves the same result as the above example
fun foo() : Promise<Bar> {
    val req = Request()
    req.returnType = Boo::class.java
    req.paths("bar")

    return request(req)
}
```

See [wiki/ApiClient](https://github.com/swarmnyc/fulton-android/wiki/ApiClient) for more detail.

## FultonApiClient
FultonApiClient is an abstract class and extends from ApiClient. It is fully designed to handle the request and response for [Fulton Server](https://github.com/swarmnyc/fulton/tree/master/fulton-server) which is a RESULTFul server based on Express. For example,

``` kotlin
/* ProductApiClient handles all API calls of Product */
class ProductApiClient : FultonApiClient(){
    override val urlRoot = "https://api.your-domain.com/products"
    
    fun listProduct() : Promise<ApiManyResult<Product>> {
        return list()
    }
}

val productApiClient = ProductApiClient()
productApiClient.listAllProducts().thenUi { result ->
    listAdapter.add(result.data)
}
```

These five methods matches the RESTFul convention:
- `fun <T> list(queryParams: QueryParams?, init: (Request.() -> Unit)?): Promise<ApiManyResult<T>>`
- `fun <T> detail(id: Any, queryParams: QueryParams?, init: (Request.() -> Unit)?): Promise<T>`
- `fun <T> create(entity: T, init: (Request.() -> Unit)?): Promise<T>`
- `fun <T> update(id: Any, entity: T, init: (Request.() -> Unit)?): Promise<Unit>`
- `fun delete(id: Any, init: (Request.() -> Unit)?): Promise<Unit>`

You can use FultonApiClient (even though the API Server doesn't use Fulton Server) as long as the API Server meets these specifications.
1. It is RESTFul.
1. the body of request for "create" and "update"
    ``` js
    { 
        data: T 
    }
    ```
1. the body of response for "list"
    ``` js
    {
        data: T[],
        pagination?: { size:number, index:number, total:number},
        error?: FultonError
    }
    ```
1. the body of response for "detail" and "create"
    ``` js
    {
        data: T,
        error?: FultonError
    }
    ```

See [wiki/FultonApiClient](https://github.com/swarmnyc/fulton-android/wiki/FultonApiClient) for more detail.

### QueryParams
QueryParams are the query parameters that are accepted by Fulton-Server which provide you with a flexible way to query data. 

It has 5 parts:
- filter
- sort
- projection, to define whether columns should return or not
- includes, to define whether api should extra data or not
- pagination

``` kotlin
fun listProduct(keyword:String) : Promise<ApiManyResult<Product>> {
    return list {
        queryParams {
            filter {
                "category" to "book"
                "title" to json {
                    "like" to keyword
                }
            }
            
            sort {
                desc("releaseDate")
                ase("title")
            }

            projection {
                show("price")
                hide("discount")
            }

            includes {
                add("factory")
            }

            pagination {
                index = 1
                size = 100
            }
        }
    }
}

// or 
val qp = queryParams { 
    ...
}

fun listProduct(qp:queryParams) : Promise<ApiManyResult<Product>> {
    return list(qp)
}

// it generates this query string ?filter[category]=book&filter[title][$like]=$keyword&sort=releaseDate-,title&includes=factory&pagination[index]=1&pagination[size]=100
```

See [wiki/QueryParams](https://github.com/swarmnyc/fulton-android/wiki/QueryParams) for more detail.

### Authentication
FultonApiClient can handle authentication itself. Currently, Fulton supports bearer token. There are two ways to set the token:

- Fulton.context.identityManager.token or fultonApiClient.context.identityManager.token
Put the token on the global context or on its own context. For example,

``` kotlin
fun login(username:String, password:String) : Promise<Unit> {
    return request {
        paths("login")
        body("username" to username, "password" to password)
    }.then {
        context.identityManager.token = it
    }
}
```

- IdentityApiClient
Fulton-Android provides IdentityApiClient which matches the authentication of Fulton-Server. You can extend the class and use it that way. For example,

``` kotlin
class MyIdentityApiClient : IdentityApiClient() {
    override val urlRoot = "https://api.your-domain.com/auth"
}

val identityApiClient = MyIdentityApiClient()
identityApiClient.login(username, password)
```

Once the access token has settled, all the coming API calls have `Authorization bearer ${token}` in their request headers.

See [wiki/Identity](https://github.com/swarmnyc/fulton-android/wiki/Identity) for more detail.

### Cache
ApiClient can use cached data for speeding up. The cache logics are:
- storing response body for GET method for given time (the default value is 5 mins), while the key is the url.
- When another GET request calls, ApiClient checks if there is data matched to the same url and if it hasn't expired. Then ApiClient returns the data immediately instead of making a real Request.

There are many options to change cache settings:

- set default cache duration
``` kotlin
// change the option when initialization
Fulton.init(this) {
    defaultCacheDurationMs = 60_000 // set the default cache duration to 1 minute
}

// or after initialization
Fulton.context.defaultCacheDurationMs = 60_000 
```

- set cache duration for each request
``` kotlin
fun getData() : Promise<Data> {
    return request {
        cacheDurationMs = 0 // if the duration is 0 means no cache for this request.
    }
}
```

- set the CacheManager

Fulton-Android provides two managers,
1. SqlCacheManager, the default cache manager which stores data in its database.
2. VoidCacheManager, the cache manager does nothing, but it can be used in Test.

You also can customize your own cache manager. For example,

``` kotlin
class MyCacheManager : CacheManager {
    override fun add(cls: String, url: String, durationMs: Int, data: ByteArray) {
        ...
    }

    override fun <T> get(url: String, type: Type): T? {
        ...
    }

    override fun clean(cls: String?) {
        ...
    }
}

// to set the cache manager
Fulton.init(this) {
    cacheManager = MyCacheManager()
}
```

### Error Handling
Fulton-Android has three places to catch the errors by request:

1. apiClient.onError (error: Throwable): Boolean
With the error handler on ApiClient, the return value tells us if the error is handled or not. If the value is "true", it means the error is handled, so .catch or Fulton.context.errorHandler won't be invoked. For example,

``` kotlin
class MyApiClient : ApiClient() {
    override fun onError(error: Throwable): Boolean {
        ... // do some things
        return error is MyException
    }
}
```

2. .catch or .catchUi
Every request returns a Promise object. It supports using .catch and .catchUi to catch errors. For example,
``` kotlin
FooApiClient().barList().then {
    ...
}.catch {
    // handle the error in normal thread
}.catchUi 
    // handle the error in UI thread
}
```

3. global error handler
You can add a global error handler to catch all unhandled errors from your requests. For example:

``` kotlin
Fulton.init(this) {
    errorHandler = { error ->
        // do some things, like logging the error or show a dialog
    }
}
```

There are two ways to avoid errors through the global error handler:
- handle errors on apiClient.onError which was mentioned above, or
- set request.shouldSendErrorToErrorHandler = false

For example,
``` kotlin
fun getData() : Promise<Data> {
    return request {
        shouldSendErrorToErrorHandler = false
    }
}
```

Fulton.context.errorHandler only catches errors from ApiClient. If errors are from .then, .thenUi or other promise methods, the errors are not caught, so these will go to Promise.uncaught. For example:

``` kotlin
FooApiClient().barList().then {
    throw error1
}.catch {
    // error1 is caught there
    throw error2
}

FooApiClient().barList().then {
    throw error3
}

Promise.uncaughtError = {
    // error2 and error3 are caught there
}
```

## FultonContext
FultonContext stores options and the need for other objects like ApiClient. There is a global FultonContext created on Fulton.init. By default, it is used if you don't give a specific context. ApiClient and FultonApiClient will use the global context. However, Each ApiClient and FultonApiClient can have its own context. For example:

**use default FultonContext class**
``` kotlin
// create a default FultonContext by Fulton.createDefaultContext
class MyApiClient(context: FultonContext): ApiClient(context)
{
    ....
}

fun foo() {
    val myContext = Fulton.createDefaultContext(this) {
        ...
    }

    val myApiClient = MyApiClient(myContext)
}

// or you can create your own context
class MyContext : FultonContext {
    ....
}

class MyApiClient(): ApiClient(MyContext())
{
    ....
}

```

This feature only supports if the devices is Android 7.0 or above. Also, it stops monitoring if the app go to background and starts monitoring if the app come back to foreground.

## Direct Use
If you want to make a simple request, you can use Fulton.request. For example:

``` kotlin
Fulton.request<Foo> {
    // provide every parameters that request needs
    urlRoot = "http://api.your-domain.com"
    paths("bar")

    resultType = Foo::class.java
}.then {
    ...
}
```

## For Test
Fulton-Android supports a way to mock responses. For example:
``` kotlin
// once requestExecutorMock is not null, ApiClient will use it to execute the request
Fulton.context.requestExecutorMock = object : RequestExecutor(){
    override fun execute(req: Request, callback: RequestCallback) {
        when (req.url){
            "https://api.your-domain.com/foo"-> {
                // return a list
                callback(req, Response(200, listOf(Foo(), Foo())))
            } 
            "https://api.your-domain.com/bar"->{
                // return a object
                callback(req, Response(200, Foo()))
            }
        }
    }
}

MyApiClient().foo().then {
    // the result from the MockRequestExecutor object
}

Fulton.context.mockRequestExecutor = null // to cancel mocking
```

## Best Practice 
We've provided a sample app to show you how we use Fulton-Android with others libraries, as seen below: 

See [sample](https://github.com/swarmnyc/fulton-android/tree/master/sample) for more information.
