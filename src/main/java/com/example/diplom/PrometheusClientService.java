package com.example.diplom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class PrometheusClientService {

    private final RestTemplate restTemplate;
    private final String prometheusBaseUrl;
    private final TaskExecutionProperties taskExecutionProperties;

    public PrometheusClientService(@Value("${prometheus.url}") String prometheusUrl, TaskExecutionProperties taskExecutionProperties) {
        this.prometheusBaseUrl = prometheusUrl;
        this.restTemplate = new RestTemplate();
        this.taskExecutionProperties = taskExecutionProperties;
    }

    /**
     * Повертає середній час відповіді за заданий період.
     * @param duration - тривалість у форматі PromQL, наприклад "1m" або "5m".
     * @return середній час відповіді у секундах.
     */
    public double getAverageResponseTime(String duration) {
        String query = "sum(avg_over_time(http_server_requests_seconds_sum[" + duration + "]))  " +
        "/ sum(avg_over_time(http_server_requests_seconds_count[" + duration + "]))";
        return executeQuery(query);
    }

    public double getPercentileResponseTime(double percentile, String duration) {
        String query = "histogram_quantile(" + (percentile / 100) +
                ", sum(rate(http_server_requests_seconds_bucket[" + duration + "])) by (le))";
        return executeQuery(query);
    }

    public double getAvgForEndpoint(String duration, String endpoint) {

        try {
            String query = URLEncoder.encode("avg_over_time(http_server_requests_seconds_sum[" + duration + "]) / avg_over_time(http_server_requests_seconds_count[" + duration + "])", StandardCharsets.UTF_8);
            URL url = new URL("http://localhost:9090/api/v1/query?query=" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(connection.getInputStream());

            JsonNode results = jsonResponse.get("data").get("result");
            connection.disconnect();
            for (JsonNode metricEntry : results) {
                String uri = metricEntry.get("metric").get("uri").asText();

                if (uri.equals(endpoint)) {
                    return metricEntry.get("value").get(1).asDouble();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1.0; // Якщо значення не знайдено
    }

    public double getPercForEndpoint(String duration, double percentile, String endpoint) {

        try {
            URL url = new URL("http://localhost:9090/api/v1/query?query=histogram_quantile(" + (percentile / 100) + ", rate(http_server_requests_seconds_bucket[" + duration + "]))");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(connection.getInputStream());

            JsonNode results = jsonResponse.get("data").get("result");
            connection.disconnect();
            for (JsonNode metricEntry : results) {
                String uri = metricEntry.get("metric").get("uri").asText();

                if (uri.equals(endpoint)) {
                    return metricEntry.get("value").get(1).asDouble();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1.0; // Якщо значення не знайдено
    }
    /**
     * Повертає відсоток помилкових запитів за заданий період.
     * @param duration - тривалість у форматі PromQL, наприклад "1m" або "5m".
     * @return error rate у відсотках.
     */
    /*public double getErrorRate(String duration) {
        String query = "(sum(rate(http_server_requests_seconds_count{status=~\"5..\"}[" + duration + "])) / sum(rate(http_server_requests_seconds_count[" + duration + "])))*100";
        return executeQuery(query);
    }*/

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
        String url = prometheusBaseUrl + "/api/v1/query?query=" + query;
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