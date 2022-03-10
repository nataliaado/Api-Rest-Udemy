package br.com.nataliaado.rest;

import io.restassured.RestAssured;
import io.restassured.internal.path.xml.NodeImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserXMLTest {

    @BeforeClass
    public static void setUp() {
        RestAssured.basePath = "https://restapi.wcaquino.me";
        //RestAssured.port = 443;
        //RestAssured.basePath = "";
    }

    @Test
    public void devoTrabalharComXML() {
        given()
                .log().all()
                .when()
                .get("/usersXML/3")
                .then()
                .statusCode(200)

                .rootPath("user")
                .body("name", is("Ana Julia"))
                .body("@id", is("3"))

                .rootPath("user.filhos")
                .body("name.size()", is(2))

                .detachRootPath("filhos")
                .body("filhos.name[0]", is("Zezinho"))
                .body("filhos.name[1]", is("Luizinho"))

                .appendRootPath("filhos")
                .body("name", hasItem("Luizinho"))
                .body("name", hasItems("Luizinho", "Zezinho"));
    }


    @Test
    public void devoFazerPesquisasAvancadasComXML() {
        given()
                .when()
                .get("/usersXML")
                .then()
                .statusCode(200)
                .body("user.user.size()", is(3))
                .body("users.user.findAll{it.age.toInteger() <= 25}.size()", is(2))
                .body("users.user.@id", hasItems("1", "2", "3"))
                .body("users.user.find{it.age == 25}.name", is("Maria Joaquina"))
                .body("users.user.findAll{it.name.toString().contains('n')}.name", hasItems("Maria Joaquina", "Ana Julia"))
                .body("users.user.salary.find{it != null}", is("1234.5678"))
                .body("users.user.age.collect{it.toInteger() * 2}", hasItems(40, 50, 60))
                .body("users.user.name.findAll{it.toString().startsWith('Maria')}.collect{it.toString().toUpperCase()}", is("MARIA JOAQUINA"));
    }

    @Test
    public void devoFazerPesquisasAvancadasComXMLEJava() {
        ArrayList<NodeImpl> nomes = given()
                .when()
                .get("/usersXML")
                .then()
                .statusCode(200)
                .extract().path("users.user.name.findAll{it.toString().contains('n')}");
        assertEquals(2, nomes.size());
        assertEquals("Maria Joaquina".toUpperCase(), nomes.get(0).toString().toUpperCase());
        assertTrue("ANA JULIA".equalsIgnoreCase(nomes.get(1).toString()));

    }

    @Test
    public void devoFazerPesquisasAvancadasComXPath() {
        given()
                .when()
                .get("/usersXML")
                .then()
                .statusCode(200)
                .body(hasXPath("count(/users/user)", is("3")))
                .body(hasXPath("/users/user[@id = '1']"))
                .body(hasXPath("//user[@id = '2']"))
                .body(hasXPath("//name[text() = 'Luizinho']/../../name", is("Ana Julia")))
                .body(hasXPath("//name[text() = 'Ana Julia']/following-sibling::filhos", allOf(containsString("Zezinho"), containsString("Luizinho"))))
                .body(hasXPath("//name", is("João da Silva")))
                .body(hasXPath("/users/user[2]/name", is("Maria Joaquina")))
                .body(hasXPath("/users/user[last()]/name", is("Ana Julia")))
                .body(hasXPath("//user[age < 24]/name", is("Ana Julia")))
                .body(hasXPath("//user[age > 20 and age < 30]/name", is("Maria Joaquina")));
    }
}
