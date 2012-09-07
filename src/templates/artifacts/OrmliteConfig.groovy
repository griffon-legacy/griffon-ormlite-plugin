database {
    driverClassName = 'org.h2.Driver'
    username = 'sa'
    password = ''
}
environments {
    development {
        database {
            url = 'jdbc:h2:mem:@griffon.project.key@-dev'
        }
    }
    test {
        database {
            url = 'jdbc:h2:mem:@griffon.project.key@-test'
        }
    }
    production {
        database {
            url = 'jdbc:h2:mem:@griffon.project.key@-prod'
        }
    }
}
