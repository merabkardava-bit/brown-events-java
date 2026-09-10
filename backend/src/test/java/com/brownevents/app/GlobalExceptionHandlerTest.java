package com.brownevents.app;

import com.brownevents.app.exception.RegistrationMismatchException;
import com.brownevents.app.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Conference not found");
        }
        @GetMapping("/test/mismatch")
        public void throwMismatch() {
            throw new RegistrationMismatchException("Registration does not belong to this conference");
        }
        @GetMapping("/test/unexpected")
        public void throwUnexpected() {
            throw new RuntimeException("secret internal details");
        }
    }

    @Test
    public void notFound_shouldReturn404WithErrorShape() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Conference not found"));
    }

    @Test
    public void mismatch_shouldReturn400WithErrorShape() throws Exception {
        mockMvc.perform(get("/test/mismatch"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Registration does not belong to this conference"));
    }

    @Test
    public void unexpectedException_shouldReturn500WithSafeMessage() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    public void unexpectedException_shouldNotExposeInternalMessage() throws Exception {
        // "secret internal details" must not appear anywhere in the response
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret internal details"))));
    }
}
