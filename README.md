# SBT Pipeline Plugin

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Build Status](https://jenkins-prod.api-platforms.telegraph.co.uk/job/Pipeline/job/sbt-pipeline-plugin/badge/icon)](https://jenkins-prod.api-platforms.telegraph.co.uk/job/Pipeline/job/sbt-pipeline-plugin/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/53854f7a89b847cba05502c382573abe)](https://www.codacy.com/app/telegraph/sbt-pipeline-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=telegraph/sbt-pipeline-plugin&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/53854f7a89b847cba05502c382573abe)](https://www.codacy.com/app/telegraph/sbt-pipeline-plugin?utm_source=github.com&utm_medium=referral&utm_content=telegraph/sbt-pipeline-plugin&utm_campaign=Badge_Coverage)

This SBT plugin is part of CI/CD Pipeline project and it was created in order to have an uniform way to deploy,
test and destroy the supporting infrastructure for our services. Therefore, the plugin must be:
 * *Highly configurable* - the plugin needs to support all parameters needed to deploy an AWS Stack;
 * *Simple to configure* - the plugin should be able to collect most of the information from the project itself;
 * *Simple to use* - all tasks should be executed via sbt commands;

## Parameters
In order to fulfill the first requirement, it is possible to configure the following parameters:

### stackName
This **String** parameter is used to specify the stack name.  

Default: "*projectName.replace("-service", "")*-*stackEnv*"

### stackEnv
This **String** parameter is used to specify the environment name. The available environments by default 
are: *static*, *dev*, *preprod* or *prod*.

Default: -

### stackParamsPath
This **File** parameter sets the stack parameters folder.

Default: -
 
### stackTemplatePath
This **File** parameter sets the stack templates folder.

Default: -

### stackSkip
This **Boolean** parameter allow us to skip a specific stack configuration deployment. Its value depends on 
the selected **Configuration**.

Default: -

### stackCustomParams
This **Map** parameter allow us to set custom stack parameters which will then be merged with the ones collected 
from *stackParamsPath*. This property giving us the possibility to generate stack parameters dynamically.
If the same property is found in *stackCustomParams* and in the file pointed by *stackParamsPath* the custom one 
will prevail. 

Default: **Map.empty**
 
### stackTags
This **Map** parameter allow us to specify stack tags to be used.

Default: **Map(Billing -> Platforms)**
 
### stackCapabilities 
This **Set[String]** parameter allow us to specify stack capabilities.

Default: **Set(CAPABILITY\_IAM, CAPABILITY\_NAMED\_IAM)**

### stackRegion  
This **String** allow us to set the region where the stack should be deployed.

Default: **eu-west-1**

### stackAuth         
This **AuthCredentials** parameter allow us to specify how AWS operations should be authenticated. The authentication 
process can be done via:
 * AuthProfile(*profileName*) - profile based authentication. If no profileName is set, then default 
 profile will be used;
 * AuthToken(*accessToken*, *secretToken*) - token based authentication;
 * AuthEnvVars() - environment based authentication.
 
Default: **AuthProfile()** 


## Configurations
In order to accommodate different stack environments, to each environment there is a configuration. This section 
describes the defaulf configurations available:


### static
This configuration sets the stack parameters for *Static Environment* which are:
 * **stackEnv** = *"static"*
 * **stackParamsPath** = *"${baseDirectory}/infrastructure/static/parameters"*
 * **stackTemplatePath** = *"${baseDirectory}/infrastructure/static/parameters"*

Sbt Variable: *DeployStatic* 

Sbt Commandline: sbt static:*


### dev
This configuration sets the stack parameters for *Dev Environment* which are:
 * **stackEnv** = *"dev"*
 * **stackParamsPath** = *"${baseDirectory}/infrastructure/dynamic/parameters"*
 * **stackTemplatePath** = *"${baseDirectory}/infrastructure/dynamic/parameters"*

Sbt Variable: *DeployDev* 

Sbt Commandline: sbt dev:*


### preprod
This configuration sets the stack parameters for *Preprod Environment* which are:
 * **stackEnv** = *"preprod"*
 * **stackParamsPath** = *"${baseDirectory}/infrastructure/dynamic/parameters"*
 * **stackTemplatePath** = *"${baseDirectory}/infrastructure/dynamic/parameters"*

Sbt Variable: *DeployPreProd* 

Sbt Commandline: sbt preprod:*


### prod
This configuration sets the stack parameters for *Prod Environment* which are:
 * **stackEnv** = *"prod"*
 * **stackParamsPath** = *"${baseDirectory}/infrastructure/dynamic/parameters"*
 * **stackTemplatePath** = *"${baseDirectory}/infrastructure/dynamic/parameters"*

Sbt Variable: *DeployProd* 

Sbt Commandline: sbt prod:*


## Tasks
The available tasks in the plugin are:
* **stackSetup** - This task is a combination of several tasks and is able to publish all templates to a specific 
S3 Bucket, create or update the stack.
* **stackTest** - This task triggers the stack tests (currently CI tests);
* **stackTeardown** - This task is a rename of **stackDelete**;
* **stackDescribe** - This task queries AWS Cloudformation and returns information about the stack;
* **stackCreate** - This task uses AWS SDK to create a stack based on plugin's configuration;
* **stackDelete** - This task uses AWS SDK to delete a stack;
* **stackUpdate** - This task uses AWS SDK to update a task;  
* **stackPublish** - This task uses AWS SDK to publish templates to S3 Bucket ("s3://artifacts-repo/${projectName}/${projectVersion}/cloudformation/"). 


#Examples
Check the available templates to see how we can configure these parameters on SBT.
