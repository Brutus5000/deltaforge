package net.brutus5000.deltaforge;

import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.model.Repository;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class Utils {
    private Utils() {
        // static class
    }

    public Repository mockMvcTestRepository(MockMvc mockMvc) {
        return null;
    }

    public void assertApiError(MvcResult mvcResult, ErrorCode errorCode) throws Exception {
        JSONObject resonseJson = new JSONObject(mvcResult.getResponse().getContentAsString());
        JSONAssert.assertEquals(String.format("{\"errors\":[{\"code\":\"%s\"}]}", errorCode.getCode()), resonseJson, false);
    }
}
