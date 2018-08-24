# Best Practice of Fulton-Android

## Other Libraries
- [Koin](https://github.com/InsertKoinIO/koin), a pragmatic lightweight dependency injection framework for Kotlin.
- [PromiseKt](https://github.com/swarmnyc/PromiseKt), a simple and easy to use of Promise library for Kotlin on JVM and Android.

## Use Fulton-Server-Example
We have provided an docker image of [fulton-server-example](https://github.com/swarmnyc/fulton-server-example) that provide an API server for this sample app. 

To launch the server

``` bash
# pull and run fulton-server-example
docker run --rm -p 3000:3000 wadehuang36/fulton-server-example
```

ake a TCP tunnel from the android Device to the local machine.
``` bash
adb reverse tcp:3000 tcp:3000
```

Therefore, call http://localhost:3000 on the android device, it actually calls http://the-local-machine:3000. So the API address can be localhost instead of the actual ip of the local machine.

## Architecture

### ApiClient and Service 
![ApiClient](/.assets/apiclient-and-service.png)<br/>The picture the architecture of ApiClient

The architecture we use for ApiClient are

1. make an ApiClient match a router of an API server. For example, if there is ProductRouter on the api server and it has three actions. Then, we create a ProductApiClient with these three actions.

2. make a Service with serial related ApiClients. For example, if there are ProductApiClient, CategoryApiClient and SupplierApiClient and groups these three ApiClients to ProductService

3. The customers of an ApiClient are only Services, The customers of a Service are such as Activity and Activity doesn't access ApiClients directly.

### Interface and Implement
Create interfaces for ApiClient and Service, so it is easier for testing. For example,
``` kotlin
interface ProjectApiClient {
    fun listProduct():Promise<List<Product>>
    fun getProduct(id:String):Promise<Product>
}

class ProjectApiClientImpl : FultonApiClient, ProjectApiClient { ... }
```

### Module
Use koin or other Dependency Injection frameworks to manage modules. For example,
``` kotlin
val productModule = applicationContext {
    bean { ProductApiClientImpl() as ProductApiClient }
    bean { CategoryApiClientImpl() as CategoryApiClient }
    bean { SupplierApiClientImpl() as SupplierApiClient }
    bean { ProductServiceImpl() as ProductService }
}

val customerModule = applicationContext { ... }

val orderModule = applicationContext { ... }

class SampleApplication : Application() {
    override fun onCreate() {
        StandAloneContext.startKoin(listOf(productModule, customerModule, orderModule)).with(this)
    }
}
```

### File Structure
We suggest the file structure separates by modules, like below

``` text
root
├── app
|   ├── src
|   |   ├── product-module
|   |   |   ├── provider
|   |   |   |   ├── ProductApiClient.kt
|   |   |   |   ├── CategoryApiClient.kt
|   |   |   |   └── ProductService.kt
|   |   |   ├── model
|   |   |   |   ├── ProductModel.kt
|   |   |   |   └── CategoryModel.kt
|   |   |   ├── view
|   |   |   |   ├── ProductListActivity.kt
|   |   |   |   └── ProductDetailActivity.kt
|   |   ├── order-module
|   |   |   ├── provider
|   |   |   |   ├── OrderApiClient.kt
|   |   |   |   └── OrderService.kt
|   |   |   ├── model
|   |   |   |   ├── OrderModel.kt
|   |   |   |   └── OrderItemModel.kt
|   |   |   ├── view
|   |   |   |   ├── OrderActivity.kt
|   |   |   |   └── OrderReviewActivity.kt
|   |   └── Application.kt
|   └── build.gradle
└── build.gradle
```
