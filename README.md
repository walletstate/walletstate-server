# WalletState.online
State of your wallet online

## Motivation
- try to use the ZIO ecosystem on some real project
- get some experience with Scala 3
- as a bonus: create a tool for the semi-automatic gathering of data about financial transactions from various sources and visualizing aggregated data using custom-defined queries

## Demo
- Run `docker-compose up` in the root directory of the project 
- Open the `localhost:8080` URL in your browser 
- Use credentials `demo`:`demo` to log in as a demo user.

## TODO list
The current version of server has a very basic functionality needed to build some MVP version of a product and requires a lot of improvements. Some of them:
- [ ] add validation of input data
- [ ] add proper error handling
- [ ] add access right verification (verify that the user edits entities in a current wallet)
- [ ] improve configuration management
- [ ] cover functionality by tests
- [ ] improve logging: add user and wallet ids to the access logs; add trace id to the logs
- [ ] add the ability to use a 'Bearer' token to access an API programmatically (currently implemented only Cookies-based auth for frontend (so-called Backend for frontend))
- [ ] implement integration with identity provider (SSO/SAML) (currently implemented preconfigured users only)
- [ ] implement querying and aggregation of data by user-defined filters for further visualization and analytics

## Angular http client generation PoC
Regarding the choice of technologies, I prefer to use tools most suitable for specific tasks. Hence, to build the [front-end part of the application](https://github.com/walletstate/walletstate-ui) was chosen TypeScript (and Angular framework) instead of Scala.js. Angular designed to build front-end applications and PWAs, has a lot of libraries, and quite big community. Also, it has almost all the necessary tools out of the box and was initially designed to be used with TypeScript.

The downside of this choice is the duplication of the code in back-end and front-end projects, but it can be resolved by code generation. So was decided to generate Angular HttpClients and data models based on back-end code and use them as [a library in the front-end project](https://github.com/walletstate/walletstate-ui/blob/v0.0.1/package.json#L34).

One of the ways to implement the code generation is to generate an OpenAPI spec (`zio-http` already supports it) and then generate TypeScript code based on the OpenAPI spec (using some existing tools). 

But, as one of the goals of this project is to get experience with the ZIO ecosystem, the Angular code generation is implemented based directly on the `zio-http` endpoints and `zio-schema` of the models. The current implementation is just a PoC, covers only current project needs, and has a lot of limitations and requirements, but it works.

Limitations and requirements:
- generated Angular HttpClients handle only happy paths, there are no error-handling
- angular CLI must be installed on a host machine (library generator runs angular CLI commands to generate angular project and build library)
- a source code for generating the Angular library is located in the `zio.http` package because it needs access to some private classes of the `zio-http`
- code generation runs from a separate `Main` app (`zio.http.gen.GenPlayground`) (currently I just run it using a button from IDE, but ideally it should be some SBT task or plugin)
- authorization is not covered by generated code (authorization is implemented based on cookies, so any additional auth logic is not needed )
- generic classes: code generation works only for generic classes with one generic field (I haven't found how to determine, based on a schema, which field of the class is generic, so for now just added custom annotation `@genericField()` to mark generic field)

## Scaling

Service is developed to run on the "home lab" server for personal use but with the ability to scale in mind.
Assume that we need to scale service to handle a large number of wallets, but each separate wallet can be handled by a single instance. In this case, the following steps should be done:
- move `users` to a separate database/service
- use some file storage, instead of DB, to store icons
- implement horizontal sharding based on wallet id 
