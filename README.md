# TwitterTrends

Twitter trends application visualizes frequency of hashtags from public twitter stream.

# How To Run
```
$ make package
$ cp docker-compose.yml docker-compose.yml-prod
# Edit docker-compose.yml-prod and insert twitter API keys
$ docker-compose -f docker-compose.yml-prod up
```

Then navigate to http://yourdockerhost:8000
