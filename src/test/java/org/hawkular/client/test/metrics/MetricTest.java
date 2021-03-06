/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.client.test.metrics;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.ProcessingException;

import org.hawkular.client.core.ClientResponse;
import org.hawkular.client.core.DefaultClientResponse;
import org.hawkular.client.core.HawkularClient;
import org.hawkular.client.core.jaxrs.Empty;
import org.hawkular.client.core.jaxrs.ResponseCodes;
import org.hawkular.client.test.BaseTest;
import org.hawkular.client.test.utils.DataPointGenerator;
import org.hawkular.client.test.utils.MetricGenerator;
import org.hawkular.client.test.utils.RandomStringGenerator;
import org.hawkular.client.test.utils.TagGenerator;
import org.hawkular.metrics.model.AvailabilityType;
import org.hawkular.metrics.model.Metric;
import org.hawkular.metrics.model.MetricType;
import org.hawkular.metrics.model.MixedMetricsRequest;
import org.hawkular.metrics.model.param.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"metrics"})
public class MetricTest extends BaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(MetricTest.class);

    private final DataPointGenerator<AvailabilityType> dataPointGenerator = new DataPointGenerator<AvailabilityType>() {
        private List<AvailabilityType> values = Arrays.asList(AvailabilityType.DOWN, AvailabilityType.UP, AvailabilityType.UNKNOWN);
        private AtomicInteger counter = new AtomicInteger(0);

        @Override
        protected AvailabilityType getValue(Random random) {
            if (counter.get() >= values.size()) {
                counter.set(0);
            }

            int index = counter.getAndIncrement();
            return values.get(index);
        }
    };

    private Integer originalMetricCount = 0;
    private final String metricName = RandomStringGenerator.getRandomId();
    private final Tags tags = TagGenerator.generate(RandomStringGenerator.getRandomId(), RandomStringGenerator.getRandomId());
    private final Metric<AvailabilityType> expectedMetric = MetricGenerator.generate(MetricType.AVAILABILITY, tags.getTags(), metricName, dataPointGenerator.generator(3, tags.getTags()));

    @Test
    public void findMetricsCount() {
        ClientResponse<List<Metric<?>>> response = client()
            .metrics()
            .metric()
            .findMetrics(MetricType.AVAILABILITY, tags, metricName);

        if (response.getStatusCode() == ResponseCodes.NO_CONTENT_204.value()) {
            Assert.assertTrue(true);
        } else {
            Assert.assertTrue(response.isSuccess());
            Assert.assertNotNull(response.getEntity());
            Assert.assertTrue(response.getEntity().size() > 0);

            originalMetricCount = response.getEntity().size();
        }
    }

    @Test(dependsOnMethods = "findMetricsCount")
    public void createMetric() {
        LOG.info("Testing with MetricName == {}", metricName);

        ClientResponse<Empty> response = client()
            .metrics()
            .metric()
            .createMetric(true, expectedMetric);

        Assert.assertTrue(response.isSuccess());
    }

    @Test(dependsOnMethods = "createMetric")
    public void findMetrics() {
        ClientResponse<List<Metric<?>>> response = client()
            .metrics()
            .metric()
            .findMetrics(MetricType.AVAILABILITY, tags, metricName);

        Assert.assertTrue(response.isSuccess());
        Assert.assertNotNull(response.getEntity());
        Assert.assertTrue(response.getEntity().size() > 0);
        Assert.assertTrue(originalMetricCount == (response.getEntity().size() - 1));

        Optional<Metric<?>> value = response.getEntity().stream()
            .filter(a -> a.equals(expectedMetric))
            .findFirst();

        Assert.assertTrue(value.isPresent());
    }

    @Test(dependsOnMethods = "findMetrics")
    public void addMetricsData() {
        Map<String, String> tags = TagGenerator.generateMap(RandomStringGenerator.getRandomId(), RandomStringGenerator.getRandomId());
        Metric<AvailabilityType> metric = MetricGenerator.generate(MetricType.AVAILABILITY, tags, metricName, dataPointGenerator.generator(3, tags));
        MixedMetricsRequest request = new MixedMetricsRequest(null, Arrays.asList(metric), null, null);

        ClientResponse<Empty> response = client()
            .metrics()
            .metric()
            .addMetricsData(request);

        Assert.assertTrue(response.isSuccess());
    }

    @Test(dependsOnMethods = "findMetrics")
    public void findMetricsAfterOtherClientCreated() throws URISyntaxException {
        // Make sure there's no breaking static config
        HawkularClient otherClient = HawkularClient.builder("other tenant")
                .uri("http://localhost:8080/fake")
                .build();

        ClientResponse<List<Metric<?>>> response = client()
                .metrics()
                .metric()
                .findMetrics(MetricType.AVAILABILITY, tags, metricName);

        Assert.assertTrue(response.isSuccess());

        ClientResponse<List<Metric<?>>> otherClientReponse = new DefaultClientResponse<>();
        try {
            otherClientReponse = otherClient
                .metrics()
                .metric()
                .findMetrics(MetricType.AVAILABILITY, tags, metricName);
        } catch (ProcessingException ex) {
            //If nothing is listening, you get a RESTEasy error
        }

        Assert.assertFalse(otherClientReponse.isSuccess());
    }

    @Test(dependsOnMethods = "findMetrics")
    public void findMetricsTags() {
        ClientResponse<Map<String, List<String>>> response = client()
            .metrics()
            .metric()
            .findMetricsTags(tags, MetricType.AVAILABILITY);

        Assert.assertTrue(response.isSuccess());
        Assert.assertNotNull(response.getEntity());
        Assert.assertTrue(response.getEntity().size() > 0);
        Assert.assertEquals(response.getEntity(), TagGenerator.convert(tags.getTags()));
    }
}
