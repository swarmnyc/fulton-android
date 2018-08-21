package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.FultonContext
import com.swarmnyc.fulton.android.error.FultonError
import com.swarmnyc.fulton.android.error.HttpError
import com.swarmnyc.promisekt.Promise

/**
 * Api Client for Fulton which is restFul styles api
 * */
abstract class FultonApiClient(context: FultonContext = Fulton.context) : ApiClient(context) {
    protected val identityManager get() = context.identityManager

    override fun initRequest(req: Request) {
        if (identityManager.isValid()) {
            identityManager.token!!.apply {
                if (tokenType.toLowerCase() == "bearer") {
                    identityManager.token!!.apply {
                        req.headers("Authorization" to "$tokenType $accessToken")
                    }
                }
            }
        }
    }

    /**
     * make a GET request to get list of entities, the result have to be { data : T } format
     */
    protected inline fun <reified T> get(queryParams: QueryParams? = null, noinline init: (Request.() -> Unit)? = null): Promise<T> {
        return request {
            // override the resultType because result is { data : T }, but convert to T, so when using can skip .data
            this.resultType = ApiOneResult::class.java
            this.resultTypeGenerics(T::class.java)

            this.queryParams = queryParams

            if (init != null) this.init()
        }
    }

    /**
     * make a GET request to get list of entities
     */
    protected inline fun <reified T> post(queryParams: QueryParams? = null, noinline init: (Request.() -> Unit)? = null): Promise<T> {
        return request {
            this.method = Method.POST
            this.queryParams = queryParams

            if (init != null) this.init()
        }
    }

    /**
     * make a GET request to get list of entities
     */
    protected inline fun <reified T> list(queryParams: QueryParams? = null, noinline init: (Request.() -> Unit)? = null): Promise<ApiManyResult<T>> {
        return request {
            this.resultTypeGenerics(T::class.java)

            this.queryParams = queryParams

            if (init != null) this.init()
        }
    }

    /**
     * make a GET request to get a single entity
     */
    protected inline fun <reified T> detail(id: Any, queryParams: QueryParams? = null, noinline init: (Request.() -> Unit)? = null): Promise<T> {
        return request {
            // override the resultType because result is { data : T }, but convert to T, so when using can skip .data
            this.resultType = ApiOneResult::class.java
            this.resultTypeGenerics(T::class.java)

            this.queryParams = queryParams

            if (init != null) this.init()

            this.paths(id.toString())
        }
    }

    /**
     * make POST request to create an entity
     */
    protected inline fun <reified T> create(entity: T, noinline init: (Request.() -> Unit)? = null): Promise<T> {
        return request {
            this.method = Method.POST
            this.resultType = ApiOneResult::class.java
            this.resultTypeGenerics(T::class.java)
            this.body = entity

            if (init != null) this.init()
        }
    }

    /**
     * make a Put request to update the entity
     */
    protected fun <T> update(id: Any, entity: T, init: (Request.() -> Unit)? = null): Promise<Unit> {
        return request {
            this.method = Method.PUT
            this.body = entity

            if (init != null) this.init()

            this.paths(id.toString())
        }
    }

    /**
     * make a Put request to update the partial of the entity
     */
    protected fun update(id: Any, partialEntity: Map<String, Any>, init: (Request.() -> Unit)? = null): Promise<Unit> {
        return request {
            this.method = Method.PUT
            this.body = partialEntity

            if (init != null) this.init()

            this.paths(id.toString())
        }
    }

    /**
     * make a Delete request to delete the entity
     */
    protected fun delete(id: Any, init: (Request.() -> Unit)? = null): Promise<Unit> {
        return request {
            this.method = Method.DELETE

            if (init != null) this.init()

            this.paths(id.toString())
        }
    }

    override fun createError(req: Request, res: Response): Throwable {
        return if (res.isJson) {
            FultonError(req, res)
        } else {
            HttpError(req, res)
        }
    }
}

