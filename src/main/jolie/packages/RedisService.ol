
type ConnectRequest:void{
    cluster:bool
    locations:void{
        host:string
        port:int
    }
}

type ConnectResponse:void

type WriteStringOnCacheRequest:void{
    key:string
    value:string
}

type WriteStringOnCacheResponse:void

type ReadStringsFromCacheRequest:void{
    key:string
}

type ReadStringsFromCacheResponse:string

type PushStringIntoListRequest:void{
    key:string
    value:string
    direction:string
}

type PushStringIntoListResponse:long

type PopStringFromListRequest:void{
    key:string
    direction:string
}

type PopStringFromListResponse:string

interface RedisInterface {
    RequestResponse:
     connect(ConnectRequest)(ConnectResponse),
     writeStringOnCache(WriteStringOnCacheRequest)(WriteStringOnCacheResponse),
     readStringFromCache(ReadStringsFromCacheRequest)(ReadStringsFromCacheResponse),
     pushStringIntoList(PushStringIntoListRequest)(PushStringIntoListResponse),
     popStringFromList(PopStringFromListRequest)(PopStringFromListResponse)
}



service Redis {
  
inputPort ip {
        location:"local"
        interfaces: RedisInterface
    }

foreign java {
  class: "joliex.redis.RedisConnector" 
  }
}
