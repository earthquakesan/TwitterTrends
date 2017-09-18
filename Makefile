package:
	sbt assembly
	docker build -t ermilov/twitter-trends .

config:
	cp src/main/resources/twitter_keys.conf.template src/main/resources/twitter_keys.conf
	echo "Edit src/main/resources/twitter_keys.conf"
