> [!IMPORTANT]
> THIS REPOSITORY IS DEPRECATED.
>
> Development on this webservice has been moved to: [https://github.com/identifiers-org/monorepo](https://github.com/identifiers-org/monorepo/tree/master/webservices/metadata).
> This new repository targets easier code integration and facilitated CI/CD pipeline. For any concerns, please [contact us](https://docs.identifiers.org/pages/contact) or open a issue at our [main issue board](https://github.com/identifiers-org/identifiers-org.github.io/issues).

# Overview
This Web Service implements [__identifiers.org__](http://identifiers.org) _Compact ID metadata extraction API_ for programmatic access to _metadata resolution services_.


# Running the Metadata Resolution Service
The only requirement for running the _resolution service_ is having a working installation of [Docker](http://docker.com).

Although this service is part of [__identifiers.org__](http://identifiers.org) **Satellite Deployments**, it can be run in _standalone_ mode, i.e. if you just want to bring this service up for programmatic access to metadata information associated with a given _Compact ID_.

Download, from this repo, the file named [_docker-compose-standalone.yml_](https://raw.githubusercontent.com/identifiers-org/cloud-ws-metadata/master/docker-compose-standalone.yml), and issue the following command:

> docker-compose -f docker-compose-standalone.yml up -d

This will launch the _Metadata Resolution Service_, if you'd like to stop it, just run:

> docker-compose -f docker-compose-standalone.yml down


# How to query the Metadata Resolution Service
There is a Java based library, [libapi](https://github.com/identifiers-org/cloud-libapi), that implements a client for this Web
Service.

Please, refer to its documentation on how to connect to [identifiers.org](https://identifiers.org) Web Services.


# Contributions
Documentation for developers will be coming soon, through the repository Wiki.


### Contact
Manuel Bernal Llinares
