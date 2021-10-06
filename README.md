# poc-springboot-multi-tenant

Il faut: une DB postgres
un schema public dans lequel il y a une table workspace (id int8, name varchar)
un ou plusieurs schema contenant une table user_entity (id int8, name varchar)

Ne pas oublié de configurer la db dans application.properties
