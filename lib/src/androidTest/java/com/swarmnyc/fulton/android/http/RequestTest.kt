package com.swarmnyc.fulton.android.http

import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.model.TopDogAuthor
import com.swarmnyc.fulton.android.promise.Promise
import com.swarmnyc.fulton.android.promise.Reject
import com.swarmnyc.fulton.android.promise.Resolve
import com.swarmnyc.fulton.android.util.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class RequestTest : BaseFultonTest() {
    companion object {
        val TAG = RequestTest::class.java.simpleName
    }

    @Test
    fun buildUrlTest() {
        val urlRoot = "http://api.fulton.com"

        var request = Request()
        request.urlRoot = urlRoot
        request.paths = listOf("news")
        request.buildUrl()

        assertEquals("http://api.fulton.com/news", request.url)

        request = Request()
        request.urlRoot = urlRoot
        request.paths = listOf("/news")
        request.buildUrl()

        assertEquals("http://api.fulton.com/news", request.url)

        request = Request()
        request.urlRoot = urlRoot
        request.paths = listOf("news", "id")
        request.buildUrl()

        assertEquals("http://api.fulton.com/news/id", request.url)

        request = Request()
        request.urlRoot = urlRoot
        request.paths = listOf("/news")
        request.query = mapOf("k1" to "a", "k2" to 2, "k3" to "&")
        request.buildUrl()

        assertEquals("http://api.fulton.com/news?k1=a&k2=2&k3=%26", request.url)
    }

    @Test
    fun buildQpUrlTest() {
        val urlRoot = "http://api.fulton.com"

        var request = Request()
        request.urlRoot = urlRoot
        request.paths = listOf("news")
        request.buildUrl()

        assertEquals("http://api.fulton.com/news", request.url)

        request = Request()
        request.urlRoot = urlRoot
        request.paths = listOf("/news")
        request.buildUrl()

        assertEquals("http://api.fulton.com/news", request.url)

        request = Request()
        request.urlRoot = urlRoot
        request.paths = listOf("news", "id")
        request.buildUrl()

        assertEquals("http://api.fulton.com/news/id", request.url)

        request = Request()
        request.urlRoot = urlRoot
        request.paths = listOf("/news")
        request.query = mapOf("k1" to "a", "k2" to 2, "k3" to "&")
        request.buildUrl()

        assertEquals("http://api.fulton.com/news?k1=a&k2=2&k3=%26", request.url)
    }

    @Test
    fun queryParamsTest() {
        val request = Request()
        val date = Date()
        val queryParams =
                queryParams {
                    filter {
                        "\$or" to listOf(json {
                            "title" to "manager"
                        }, json {
                            "title" to json {
                                "\$regex" to "sales"
                                "\$options" to "i"
                            }
                        })
                        "age" to 37
                        "male" to true
                        "birth" to json {
                            "\$lte" to date
                        }
                        "city" to json {
                            "\$in" to listOf("NY", "NJ")
                        }
                    }

                    // two styles
                    pagination {
                        index = 1
                        size = 100
                    }

                    projection = mapOf("pro1" to true, "pro2" to false)
                    includes = listOf("include1", "include2.include2")
                    sort = mapOf("sort1" to true, "sort2" to false)
                }

        request.urlRoot = "http://api.fulton.com/"
        request.queryParams = queryParams

        request.buildUrl()

        val q = date.toJson().replace("\"", "")

//        println(request.url)
        assertEquals("http://api.fulton.com/?filter[\$or][][title]=manager&filter[\$or][][title][\$regex]=sales&filter[\$or][][title][\$options]=i&filter[age]=37&filter[male]=true&filter[birth][\$lte]=$q&filter[city][\$in][]=NY&filter[city][\$in][]=NJ&sort=sort1,sort2-&projection=pro1,pro2-&include=include1,include2.include2&pagination[index]=1&pagination[size]=100", request.url)
    }

    @Test
    fun queryParams2Test() {
        var request: Request? = null
        val date = Date()
        val apiClient = object : ApiClient() {
            override val urlRoot: String = "http://api.fulton.com/"

            fun test(): Promise<Unit> {
                return request {
                    queryParams {
                        filter {
                            "\$or" to listOf(json {
                                "title" to "manager"
                            }, json {
                                "title" to json {
                                    "\$regex" to "sales"
                                    "\$options" to "i"
                                }
                            })
                            "age" to 37
                            "male" to true
                            "birth" to json {
                                "\$lte" to date
                            }
                            "city" to json {
                                "\$in" to listOf("NY", "NJ")
                            }
                        }

                        // two styles
                        pagination {
                            index = 1
                            size = 100
                        }

                        projection = mapOf("pro1" to true, "pro2" to false)
                        includes = listOf("include1", "include2.include2")
                        sort = mapOf("sort1" to true, "sort2" to false)
                    }

                    mockResponse = Response(200)
                }
            }

            override fun <T> endRequest(promise: Promise<T>, req: Request, res: Response) {
                request = req
                super.endRequest(promise, req, res)
            }
        }

        apiClient.test().await()

        val q = date.toJson().replace("\"", "")

        assertEquals("http://api.fulton.com/?filter[\$or][][title]=manager&filter[\$or][][title][\$regex]=sales&filter[\$or][][title][\$options]=i&filter[age]=37&filter[male]=true&filter[birth][\$lte]=$q&filter[city][\$in][]=NY&filter[city][\$in][]=NJ&sort=sort1,sort2-&projection=pro1,pro2-&include=include1,include2.include2&pagination[index]=1&pagination[size]=100", request!!.url)
    }

    @Test
    fun queryTest() {
        var request: Request? = null
        val apiClient = object : ApiClient() {
            override val urlRoot: String = "http://api.fulton.com/"

            fun test(): Promise<Unit> {
                return request {
                    queryParams {
                        filter {
                            "age" to 37
                            "male" to true
                        }
                    }

                    query("test1" to "abc")

                    queryString = "test2=cba"

                    mockResponse = Response(200)
                }
            }

            override fun <T> endRequest(promise: Promise<T>, req: Request, res: Response) {
                request = req
                super.endRequest(promise, req, res)
            }
        }

        apiClient.test().await()

        assertEquals("http://api.fulton.com/?test1=abc&filter[age]=37&filter[male]=true&test2=cba", request!!.url)
    }

    @Test
    fun query2Test() {
        var request: Request? = null
        val apiClient = object : ApiClient() {
            override val urlRoot: String = "http://api.fulton.com/"

            fun test(): Promise<Unit> {
                return request {
                    query("test1" to "abc")

                    queryString = "&test2=cba&test3=efg"

                    mockResponse = Response(200)
                }
            }

            override fun <T> endRequest(promise: Promise<T>, req: Request, res: Response) {
                request = req
                super.endRequest(promise, req, res)
            }
        }

        apiClient.test().await()

        assertEquals("http://api.fulton.com/?test1=abc&test2=cba&test3=efg", request!!.url)
    }
}
