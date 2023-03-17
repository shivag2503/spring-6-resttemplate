package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8080";

    BeerClient beerClient;

    MockRestServiceServer mockRestServiceServer;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO dto;
    String jsonResponse;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilder.build();
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);
        dto = getBeerDTO();
        jsonResponse = objectMapper.writeValueAsString(dto);
    }

    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_LIST)
                .queryParam("beerName", "ALE")
                .build().toUri();

        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(queryParam("beerName", "ALE"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtoPage = beerClient.
                listBeers("ALE", null, null, null, null);

        assertThat(dtoPage.getContent().size()).isEqualTo(1);
    }

    @Test
    void testDeleteNotFound() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                        dto.getId()))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> {
           beerClient.deleteBeer(dto.getId());
        });

        mockRestServiceServer.verify();
    }

    @Test
    void testDeleteBeer() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                        dto.getId()))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andRespond(withNoContent());

        beerClient.deleteBeer(dto.getId());
        mockRestServiceServer.verify();
    }

    @Test
    void testUpdateBeer() {
        mockRestServiceServer.expect(method(HttpMethod.PUT))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                        dto.getId()))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andRespond(withNoContent());

        mockGetOperation();

        BeerDTO responseDTO = beerClient.updateBeer(dto);
        assertThat(responseDTO.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testCreateBeer() {


        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH)
                        .build(dto.getId());

        mockRestServiceServer.expect(method(HttpMethod.POST))
                        .andExpect(requestTo(URL +
                                BeerClientImpl.GET_BEER_LIST))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                                .andRespond(withAccepted().location(uri));

        mockGetOperation();

        BeerDTO responseDTO = beerClient.saveBeer(dto);
        assertThat(responseDTO.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testBeerById() {
        mockGetOperation();

        BeerDTO responseDTO = beerClient.getBeerById(dto.getId());
        assertThat(responseDTO.getId()).isEqualTo(dto.getId());
    }

    private void mockGetOperation() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL +
                        BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
    }

    @Test
    void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_LIST))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    BeerDTO getBeerDTO() {
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .beerName("Mango Bos")
                .price(new BigDecimal("10.99"))
                .beerStyle(BeerStyle.LAGER)
                .upc("124533")
                .build();
    }

    BeerDTOPageImpl getPage() {
        return new BeerDTOPageImpl(Arrays.asList(getBeerDTO()), 1, 25, 1);
    }
}