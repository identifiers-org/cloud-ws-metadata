# Makefile based helper for the Metadata Service
# Author: Manuel Bernal Llinares <mbdebian@gmail.com>

# Environment
# Google Chrome Driver
version_latest_chrome_driver = 2.42
url_base_chrome_driver = http://chromedriver.storage.googleapis.com/
binary_linux_chromedriver = chromedriver_linux64.zip
binary_mac_chromedriver = chromedriver_mac64.zip
binary_windows_chromedriver = chromedriver_win32.zip
url_download_linux_chromedriver = $(url_base_chrome_driver)$(version_latest_chrome_driver)/$(binary_linux_chromedriver)
url_download_mac_chromedriver = $(url_base_chrome_driver)$(version_latest_chrome_driver)/$(binary_mac_chromedriver)
url_download_windows_chromedriver = $(url_base_chrome_driver)$(version_latest_chrome_driver)/$(binary_windows_chromedriver)
# Development support
container_name = identifiersorg/cloud-ws-metadata
docker_compose_development_file = docker-compose-development.yml
springboot_development_profile = development
tag_version = $(shell cat VERSION)

# default target
all: deploy

clean:
	@echo "<===|DEVOPS|===> [CLEAN] Running House Keeping tasks"
	@mvn clean > /dev/null
	@mvn versions:commit

release: deploy set_next_development_version
	@echo "<===|DEVOPS|===> [RELEASE] New Software Release, and next development version prepared"
	@git add pom.xml
	@git commit -am "Next project development version prepared"
	@git push

sync_project_version:
	@echo "<===|DEVOPS|===> [SYNC] Synchronizing project version to version '${tag_version}'"
	@mvn versions:set -DnewVersion=${tag_version}

set_next_development_version:
	@echo "<===|DEVOPS|===> [SYNC] Setting the new development version, current ${tag_version}"
	@mvn versions:set -DnewVersion=$(shell ./increment_version.sh -p ${tag_version})-SNAPSHOT

deploy: clean container_production_push
	@echo "<===|DEVOPS|===> [DEPLOY] Deploying service container version ${tag_version}"

development_env_up: chromedriver
	@echo "<===|DEVOPS|===> [ENVIRONMENT] Bringing development environment UP"
	@docker-compose -f $(docker_compose_development_file) up -d

development_env_down:
	@echo "<===|DEVOPS|===> [ENVIRONMENT] Bringing development environment DOWN"
	@docker-compose -f $(docker_compose_development_file) down

development_run_tests: development_env_up
	@echo "<===|DEVOPS|===> [TESTS] Running Unit Tests"
	@mvn -Dspring.profiles.active=$(springboot_development_profile) clean test

app_structure:
	@echo "<===|DEVOPS|===> [PACKAGE] Application"
	@mvn clean > /dev/null
	@mvn package -DskipTests
	@mkdir -p target/app/log
	@mkdir -p target/app/tmp
	@cp target/metadata-$(shell mvn help:evaluate -Dexpression=project.version | grep -v '^\[').jar target/app/service.jar

container_production_build: app_structure
	@echo "<===|DEVOPS|===> [BUILD] Production container $(container_name):$(tag_version)"
	@docker build -t $(container_name):$(tag_version) -t $(container_name):latest .

container_production_push: container_production_build
	@echo "<===|DEVOPS|===> [PUBLISH]> Production container $(container_name):$(tag_version)"
	@docker push $(container_name):$(tag_version)
	@docker push $(container_name):latest

chromedriver: tmp bin/selenium
	@echo "<===|DEVOPS|===> [CHROME DRIVERS] Ensuring drivers exist"
	@cd tmp; if [ ! -e ../bin/selenium/chromedriver-linux ]; then \
		wget $(url_download_linux_chromedriver); \
		unzip $(binary_linux_chromedriver); mv chromedriver ../bin/selenium/chromedriver-linux; fi
	@cd tmp; if [ ! -e ../bin/selenium/chromedriver-mac ]; then \
		wget $(url_download_mac_chromedriver); \
		unzip $(binary_mac_chromedriver); mv chromedriver ../bin/selenium/chromedriver-mac; fi
	@cd tmp; if [ ! -e ../bin/selenium/chromedriver.exe ]; then \
		wget $(url_download_windows_chromedriver); \
		unzip $(binary_windows_chromedriver); mv chromedriver.exe ../bin/selenium/; fi

# Folders
tmp:
	@echo "<===|DEVOPS|===> [FOLDER] Preparing temporary folder"
	@mkdir tmp

bin/selenium:
	@echo "<===|DEVOPS|===> [FOLDER] Preparing selenium folder for binaries"
	@mkdir -p bin/selenium
# END - Folders

# House keeping tasks
clean_tmp:
	@echo "<===|DEVOPS|===> [HOUSEKEEPING] Removing temporary folder"
	@rm -rf tmp

clean_bin:
	@echo "<===|DEVOPS|===> [HOUSEKEEPING] Cleaning external binaries"
	@rm -rf bin/*
	@touch bin/empty

.PHONY: all clean development_run_tests app_structure container_production_build container_production_push deploy release sync_project_version set_next_development_version clean_tmp clean_bin chromedriver
