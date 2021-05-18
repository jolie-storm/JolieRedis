from packages.RedisService import Redis
from console import Console

service Test{
    embed Redis as redis
    embed Console as console
    
    main{
        
        connect@redis({cluster = false  
                       locations << { host="localhost"  port= 6379 }}
                       )()
        writeStringOnCache@redis({ key="mykey" 
                                  value="myvalue"} )()
        readStringFromCache@redis({key="mykey"})(responseReadString)
        println@console(responseReadString)()
        
        for (counter= 0, counter < 5 ,counter++){

            pushStringIntoList@redis({key="mylist" 
                                    value= "myvalue" + counter 
                                    direction = "R"})()
        }

        for (counter= 0, counter< 5 ,counter++){
            popStringFromList@redis({key="mylist" 
                                     direction = "L"})(responseList)
            println@console(responseList)()
        }



    }
}