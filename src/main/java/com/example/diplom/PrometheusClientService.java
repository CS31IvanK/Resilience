package com.example.diplom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class PrometheusClientService {

    private final RestTemplate restTemplate;
    private final String prometheusBaseUrl;

    public PrometheusClientService(@Value("${prometheus.url}") String prometheusUrl) {
        this.prometheusBaseUrl = prometheusUrl;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Повертає середній час відповіді за заданий період.
     * @param duration - тривалість у форматі PromQL, наприклад "1m" або "5m".
     * @return середній час відповіді у секундах.
     */
    public double getAverageResponseTime(String duration) {
        String query = "avg_over_time(response_time_seconds[" + duration + "])";
        return executeQuery(query);
    }

    /**
     * Повертає відсоток помилкових запитів за заданий період.
     * @param duration - тривалість у форматі PromQL, наприклад "1m" або "5m".
     * @return error rate у відсотках.
     */
    public double getErrorRate(String duration) {
        String query = "(sum(rate(request_errors_total[" + duration + "])) / sum(rate(requests_total[" + duration + "])))*100";
        return executeQuery(query);
    }

    /**
     * Повертає середнестатистичне значення завантаження CPU.
     * Використовується метрика process_cpu_usage (значення від 0 до 1).
     * @return середнє CPU використання.
     */
    public double getCpuUsage() {
        String query = "avg(process_cpu_usage)";
        return executeQuery(query);
    }

    /**
     * Повертає середнє використання пам'яті (в байтах).
     * Використовується метрика jvm_memory_used_bytes.
     * @return середній обсяг використовуваної пам'яті.
     */
    public double getMemoryUsage() {
        String query = "avg(jvm_memory_used_bytes)";
        return executeQuery(query);
    }

    /**
     * Виконує запит до Prometheus за заданим запитом PromQL і повертає числове значення.
     * @param query - PromQL запит.
     * @return отримане числове значення або 0.0 у випадку помилки.
     */
    private double executeQuery(String query) {
        String url = prometheusBaseUrl + "/api/v1/query?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        try {
            ResponseEntity<PrometheusResponse> responseEntity = restTemplate.getForEntity(url, PrometheusResponse.class);
            PrometheusResponse response = responseEntity.getBody();
            if (response != null
                    && "success".equals(response.getStatus())
                    && response.getData() != null
                    && !response.getData().getResult().isEmpty()) {
                List<Object> valueList = response.getData().getResult().get(0).getValue();
                String valueStr = (String) valueList.get(1);
                return Double.parseDouble(valueStr);
            }
        } catch (Exception e) {
            System.err.println("Помилка при виконанні Prometheus запиту: " + e.getMessage());
        }
        return 0.0;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrometheusResponse {
        private String status;
        private Data data;

        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public Data getData() {
            return data;
        }
        public void setData(Data data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty("resultType")
        private String resultType;
        @JsonProperty("result")
        private List<Result> result;

        public String getResultType() {
            return resultType;
        }
        public void setResultType(String resultType) {
            this.resultType = resultType;
        }
        public List<Result> getResult() {
            return result;
        }
        public void setResult(List<Result> result) {
            this.result = result;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("metric")
        private Map<String, String> metric;
        @JsonProperty("value")
        private List<Object> value;

        public Map<String, String> getMetric() {
            return metric;
        }
        public void setMetric(Map<String, String> metric) {
            this.metric = metric;
        }
        public List<Object> getValue() {
            return value;
        }
        public void setValue(List<Object> value) {
            this.value = value;
        }
    }
}