package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.net.http.HttpConnectTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClientImpl beerClient;

    @Test
    void testDeleteBeer() {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("Mango Bos 2")
                .price(new BigDecimal("10.99"))
                .beerStyle(BeerStyle.LAGER)
                .quantityOnHand(250)
                .upc("h55545")
                .build();

        BeerDTO dto = beerClient.saveBeer(beerDTO);

        beerClient.deleteBeer(dto.getId());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.getBeerById(dto.getId());
        });
    }

    @Test
    void testUpdateBeer() {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("Mango Bos 2")
                .price(new BigDecimal("10.99"))
                .beerStyle(BeerStyle.LAGER)
                .quantityOnHand(250)
                .upc("h55545")
                .build();

        BeerDTO dto = beerClient.saveBeer(beerDTO);

        final String newName = "Mango Bos 3";
        dto.setBeerName(newName);
        BeerDTO saveBeer = beerClient.updateBeer(dto);
        assertEquals(newName, saveBeer.getBeerName());

    }

    @Test
    void testCreateBeer() {

        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("Mango Bos")
                .price(new BigDecimal("10.99"))
                .beerStyle(BeerStyle.LAGER)
                .quantityOnHand(25)
                .upc("h55545")
                .build();

        BeerDTO saveDTO = beerClient.saveBeer(beerDTO);
        assertNotNull(saveDTO);
    }

    @Test
    void getBeerById() {
        Page<BeerDTO> beerDTOS = beerClient.listBeers();
        BeerDTO beerDTO = beerDTOS.getContent().get(0);
        BeerDTO byId = beerClient.getBeerById(beerDTO.getId());
        assertNotNull(byId);
    }

    @Test
    void listBeers() {
        beerClient.listBeers("ALE", null, null, null, null);
    }
}