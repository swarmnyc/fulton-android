# Fulton Android
A simple and easy to use Android library for handling REST API or RESTFul API. It is one of [fulton](https://github.com/swarmnyc/fulton) family.

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
Fulton-Android uses Promise-Like to handle async http request. It is very familiar to Promise of JavaScript which is very easy to use. For Example,

``` kotlin
val fooApiClient = FooApiClient()
fooApiClient.barList().then { list ->
    // convert List<Bar> to List<BarViewModel>
    list.map { BarViewModel(it) }
}.thenUi { vmList ->
    // update UI
}.catchUi { error ->
    // show error
}
```

Each Api Call returns a Promise object. Then you can use `.then` to do stuff that don't require the UI thread or use `.thenUi` to update UI. Also, use `.catch` or `.catchUi` to handle error. There are many other features, see [PromiseKt](https://github.com/swarmnyc/PromiseKt) for more information.

## Api Client
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
- `inline fun <reified T> request(builder: Request.() -> Unit): Promise<T>`, The parameter is a block, that you can initialize the Request Object inside the block. 
- `fun <T> request(req: Request): Promise<T>`, The parameter is a Request object. 

We recommend you to use the first function because we've worked hard to make it easy to use. For example,

``` kotlin
fun foo() : Promise<Bar> {
    return request { // "this" is a Request object
        // initialize the Request object inside the lambda function, like
        paths("bar")
    }
}

// this serves the same result as the below example
fun foo() : Promise<Bar> {
    val req = Request()
    req.returnType = Boo::class.java
    req.paths("bar")

    return request(req)
}

```

See [wiki/ApiClient](/wiki/ApiClient) for more detail.

## Fulton Api Client
FultonApiClient is a abstract class which matches the features of EntityRouter of [fulton-server](https://github.com/swarmnyc/fulton/tree/master/fulton-server). 


QueryParams
IdentityManager
Cache
Error Handling

Best Pracitic

direct use