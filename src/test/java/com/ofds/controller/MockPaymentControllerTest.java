package com.ofds.controller;

import com.ofds.model.*;
import com.ofds.repository.PaymentRepository;
import com.ofds.service.OrderService;
import com.ofds.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class MockPaymentControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserDetails usuario;

    @Mock
    private Random random;

    @InjectMocks
    private MockPaymentController controller;

    private Order pedido;
    private Payment pagamento;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Customer cliente = new Customer();
        pedido = new Order(cliente, new Restaurant());
        pedido.setTotalAmount(100.00);
        pagamento = new Payment(pedido, PaymentMethod.CREDIT_CARD, 100.00);
        pedido.setPayment(pagamento);

        when(usuario.getUsername()).thenReturn("alex@ofds.com");
        when(userService.findCustomerByEmail("alex@ofds.com")).thenReturn(Optional.of(cliente));
        when(orderService.getCustomerOrder(1L, cliente)).thenReturn(pedido);
    }

    @Test
    void cartaoAprovadoNoSorteio() {
        when(random.nextInt(10)).thenReturn(1);

        ResponseEntity<Map<String, Object>> response = pagar("4111111111111111");

        assertEquals("success", response.getBody().get("status"));
        assertEquals(PaymentStatus.COMPLETED, pagamento.getPaymentStatus());
        verify(random).nextInt(10);
    }

    @Test
    void cartaoRecusadoNoSorteio() {
        when(random.nextInt(10)).thenReturn(0);

        ResponseEntity<Map<String, Object>> response = pagar("4111111111111111");

        assertEquals("failed", response.getBody().get("status"));
        assertEquals(PaymentStatus.FAILED, pagamento.getPaymentStatus());
        verify(random).nextInt(10);
    }

    @Test
    void cartaoDeFalhaForcada() {
        ResponseEntity<Map<String, Object>> response = pagar("0000000000000000");

        assertEquals("failed", response.getBody().get("status"));
        assertEquals(PaymentStatus.FAILED, pagamento.getPaymentStatus());
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void upiDeFalhaForcada() {
        ResponseEntity<Map<String, Object>> response = pagar("fail@upi");

        assertEquals("failed", response.getBody().get("status"));
        assertEquals(PaymentStatus.FAILED, pagamento.getPaymentStatus());
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void upiComArroba() {
        when(random.nextInt(10)).thenReturn(1);

        ResponseEntity<Map<String, Object>> response = pagar("joao@upi");

        assertEquals("success", response.getBody().get("status"));
        assertEquals(PaymentStatus.COMPLETED, pagamento.getPaymentStatus());
    }

    @Test
    void entradaComSeisCaracteres() {
        when(random.nextInt(10)).thenReturn(1);

        ResponseEntity<Map<String, Object>> response = pagar("123456");

        assertEquals("success", response.getBody().get("status"));
        assertEquals(PaymentStatus.COMPLETED, pagamento.getPaymentStatus());
    }

    @Test
    void entradaComCincoCaracteres() {
        ResponseEntity<Map<String, Object>> response = pagar("12345");

        assertEquals("failed", response.getBody().get("status"));
        assertEquals(PaymentStatus.FAILED, pagamento.getPaymentStatus());
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void quinzeZeros() {
        when(random.nextInt(10)).thenReturn(1);

        ResponseEntity<Map<String, Object>> response = pagar("000000000000000");

        assertEquals("success", response.getBody().get("status"));
        assertEquals(PaymentStatus.COMPLETED, pagamento.getPaymentStatus());
    }

    @Test
    void pedidoSemPagamento() {
        pedido.setPayment(null);

        ResponseEntity<Map<String, Object>> response = pagar("4111111111111111");

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Payment has already been processed or is unavailable.", response.getBody().get("message"));
    }

    @Test
    void pagamentoJaConcluido() {
        pagamento.makePayment();

        ResponseEntity<Map<String, Object>> response = pagar("4111111111111111");

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Payment has already been processed or is unavailable.", response.getBody().get("message"));
        assertEquals(PaymentStatus.COMPLETED, pagamento.getPaymentStatus());
    }

    @Test
    void valorUmCentavoAbaixo() {
        pagamento.setAmount(99.99);

        ResponseEntity<Map<String, Object>> response = pagar("4111111111111111");

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Payment amount does not match order total.", response.getBody().get("message"));
    }

    @Test
    void valorUmCentavoAcima() {
        pagamento.setAmount(100.01);

        ResponseEntity<Map<String, Object>> response = pagar("4111111111111111");

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Payment amount does not match order total.", response.getBody().get("message"));
    }

    private ResponseEntity<Map<String, Object>> pagar(String entrada) {
        return controller.process(1L, entrada, usuario);
    }
}
