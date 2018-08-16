# Fulton Android
A simple and easy to use Android library for handling REST API or RESTFul API. It is one of [Fulton](https://github.com/swarmnyc/fulton) family.

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
Fulton-Android uses Promise-Like to handle async http request. It is very similar to Promise of JavaScript which is very easy to use. For Example,

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

Each Api Call returns a Promise object. Then you can use `.then` to do stuff that don't require the UI thread or use `.thenUi` to update UI. Also, use `.catch` or `.catchUi` to handle error. 

There are many other features, see [PromiseKt](https://github.com/swarmnyc/PromiseKt) for more information.

## ApiClient
ApiClient is a abstract class that handles REST APIs. You can use one ApiClient to handle any requests, but we recommend that creating different ApiClients for different API routes. For Example,

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
- `fun <T> request(init: Request.() -> Unit): Promise<T>`, The parameter is a block that you can initialize the Request object inside the block. 
- `fun <T> request(req: Request): Promise<T>`, The parameter is a Request object. 

We recommend you to use the first function because it is easier to use. For example,

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
FultonApiClient is a abstract class and extends from ApiClient. It is fully designed of handling the request and response for[Fulton Server](https://github.com/swarmnyc/fulton/tree/master/fulton-server) which is a RESULTFul server based on Express. For example,

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

These five methods matches the RESTFul convention.
- `fun <T> list(queryParams: QueryParams?, init: (Request.() -> Unit)?): Promise<ApiManyResult<T>>`
- `fun <T> detail(id: Any, queryParams: QueryParams?, init: (Request.() -> Unit)?): Promise<T>`
- `fun <T> create(entity: T, init: (Request.() -> Unit)?): Promise<T>`
- `fun <T> update(id: Any, entity: T, init: (Request.() -> Unit)?): Promise<Unit>`
- `fun delete(id: Any, init: (Request.() -> Unit)?): Promise<Unit>`

You can use FultonApiClient even though the API Server doesn't use Fulton Server as long as the API Server meets these specifications.
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

### QueryParams
QueryParams is the query parameters thats accepted by Fulton Server. 

It has parts:
- filter
- sort
- projection
- includes
- pagination

``` kotlin
fun listProduct() : Promise<ApiManyResult<Product>> {
    val qp = queryParams {
                filter {
                    "category" to "book"
                }
                sort "releaseDate"

    }

    return list {}
}

```

### Authentication
See [wiki/FultonApiClient](https://github.com/swarmnyc/fulton-android/wiki/FultonApiClient) for more detail.

Cache
Error Handling

Best Pracitic

direct use