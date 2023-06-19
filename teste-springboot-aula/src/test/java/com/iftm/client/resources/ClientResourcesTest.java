package com.iftm.client.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.repositories.ClientRepository;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourcesTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ClientService clientService;

        @Autowired
        private ObjectMapper objectMapper;

        @Mock
        private ClientRepository repositorio;

        /**
         * Caso de testes : Verificar se o endpoint get/clients/ retorna todos os
         * clientes existentes
         * Arrange:
         * - camada service simulada com mockito
         * - base de dado : 3 clientes
         * new Client(7l, "Jose Saramago", "10239254871", 5000.0,
         * Instant.parse("1996-12-23T07:00:00Z"), 0);
         * new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0,
         * Instant.parse("1996-12-23T07:00:00Z"), 0);
         * new Client(8l, "Toni Morrison", "10219344681", 10000.0,
         * Instant.parse("1940-02-23T07:00:00Z"), 0);
         * - Uma PageRequest default
         */

        @DisplayName("Verificar se o endpoint get/clients/ retorna todos os clientes existentes")
        @Test
        public void testarEndPointRetornaTodosClientesExistentes() throws Exception {
                // necessário para o teste de unidade
                // confiurar mockBean Servico
                List<ClientDTO> listaClientesExistentes = new ArrayList<>();
                listaClientesExistentes.add(new ClientDTO(
                                new Client(7l, "Jose Saramago", "10239254871", 5000.0,
                                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientesExistentes
                                .add(new ClientDTO(new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0,
                                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientesExistentes.add(new ClientDTO(
                                new Client(8l, "Toni Morrison", "10219344681", 10000.0,
                                                Instant.parse("1940-02-23T07:00:00Z"), 0)));

                Page<ClientDTO> pagina = new PageImpl<>(listaClientesExistentes);
                Mockito.when(clientService.findAllPaged(Mockito.any())).thenReturn(pagina);

                int qtdClientes = 3;

                ResultActions resultado = mockMvc.perform(get("/clients/").accept(APPLICATION_JSON));
                resultado.andExpect(status().isOk())
                                .andExpect(jsonPath("$.numberOfElements").exists())
                                .andExpect(jsonPath("$.numberOfElements").value(qtdClientes));
        }

        @DisplayName("Verifica se o metodo insert retorna “created” (código 201), bem como o produto criado")
        @Test
        public void testInsertDeveRetornarCreated() throws Exception {
                ClientDTO clientDTO = new ClientDTO(10l, "Jose Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0);

                objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(clientDTO);
                ResultActions result = mockMvc.perform(post("/clients/")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isCreated());
                result.andExpect(jsonPath("$.id").exists());
                result.andExpect(jsonPath("$.name").value(clientDTO.getName()));
        }

        @DisplayName("Verificar o metodo delete, quando não encontrado o id deve retornar Not Found (fiquei em dúvida se deveria corrigir o metodo delete para ele retornar além do noContent também o NotFound)")
        @Test
        public void testDeleteDeveRetornarNaoEncontrado() throws Exception {
                Long idNaoExistente = 123240L;

                ResultActions result = mockMvc.perform(delete("/clients/{id}", idNaoExistente)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isNotFound());
        }

        @DisplayName("Verificar o metodo delete, quando o id for encontrado deve retornar No Content")
        @Test
        public void testDeleteDeveRetornarNoContent() throws Exception {
                Long IdExistente = 1L;

                ResultActions result = mockMvc.perform(delete("/clients/{id}", IdExistente)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isNoContent());
        }

        @DisplayName("Verificar o findByIncome, deve retornar código 200, bem como os clientes que tenham o Income informado")
        @Test
        public void testFindByIncomeDeveRetornarClientes() throws Exception {
                Double income = 3000.0;
                PageRequest pageRequest = PageRequest.of(0, 2, Sort.Direction.valueOf("ASC"), "income");

                List<Client> clients = new ArrayList<>();
                clients.add(new Client(1L, "John", "123456789", 3000.0, Instant.parse("2023-06-15T00:00:00Z"), 2));
                clients.add(new Client(2L, "Jane", "987654321", 3000.0, Instant.parse("2023-06-15T00:00:00Z"),
                                1));
                Page<Client> clientPage = new PageImpl<>(clients, pageRequest, clients.size());

                when(repositorio.findByIncome(income, pageRequest)).thenReturn(clientPage);

                ResultActions result = mockMvc.perform(get("/clients/income")
                                .param("income", income.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk());

                result.andExpect(jsonPath("$.content").isArray());
                result.andExpect(jsonPath("$.content.length()").value(2));

                result.andExpect(jsonPath("$.content[0].id").value(1));
                result.andExpect(jsonPath("$.content[0].name").value("John"));
                result.andExpect(jsonPath("$.content[0].cpf").value("123456789"));
                result.andExpect(jsonPath("$.content[0].income").value(3000.0));
                result.andExpect(jsonPath("$.content[0].birthDate").value("2023-06-15T00:00:00Z"));
                result.andExpect(jsonPath("$.content[0].children").value(2));

                result.andExpect(jsonPath("$.content[1].id").value(2));
                result.andExpect(jsonPath("$.content[1].name").value("Jane"));
                result.andExpect(jsonPath("$.content[1].cpf").value("987654321"));
                result.andExpect(jsonPath("$.content[1].income").value(3000.0));
                result.andExpect(jsonPath("$.content[1].birthDate").value("2023-06-15T00:00:00Z"));
                result.andExpect(jsonPath("$.content[1].children").value(1));

                result.andExpect(jsonPath("$.pageable.pageNumber").value(0));
                result.andExpect(jsonPath("$.pageable.pageSize").value(10));
                result.andExpect(jsonPath("$.totalElements").value(2));
                result.andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        public void testUpdateShouldReturnUpdatedClient() throws Exception {
                Long IdExistente = 2L;

                ClientDTO clientDTO = new ClientDTO();
                clientDTO.setName("Updated Name");
                clientDTO.setIncome(6000.0);

                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(clientDTO);

                when(clientService.update(eq(IdExistente), any(ClientDTO.class))).thenReturn(clientDTO);

                ResultActions result = mockMvc.perform(put("/clients/{id}", IdExistente)
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk());

                result.andExpect(jsonPath("$.id").value(IdExistente));
                result.andExpect(jsonPath("$.name").value("Updated Name"));
                result.andExpect(jsonPath("$.income").value(6000.0));

                result.andExpect(jsonPath("$.cpf").doesNotExist());
                result.andExpect(jsonPath("$.birthDate").doesNotExist());
                result.andExpect(jsonPath("$.children").doesNotExist());
        }

        @Test
        public void testUpdateShouldReturnNotFound() throws Exception {
                Long idNaoExistente = 999L;

                ClientDTO clientDTO = new ClientDTO();
                clientDTO.setName("Updated Name");
                clientDTO.setIncome(6000.0);

                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(clientDTO);

                when(clientService.update(eq(idNaoExistente), any(ClientDTO.class)))
                                .thenThrow(new ResourceNotFoundException("Resource not found"));
                ResultActions result = mockMvc.perform(put("/clients/{id}", idNaoExistente)
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));
                result.andExpect(status().isNotFound());
                result.andExpect(jsonPath("$.error").value("Resource not found"));
        }

}
