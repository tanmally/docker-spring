package com.kpelykh.docker.client.test;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.selectUnique;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;
import static org.testinfected.hamcrest.jpa.HasFieldWithValue.hasField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.ChangeLog;
import com.kpelykh.docker.client.model.CommitConfig;
import com.kpelykh.docker.client.model.Container;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ContainerInspectResponse;
import com.kpelykh.docker.client.model.HostConfig;
import com.kpelykh.docker.client.model.Image;
import com.kpelykh.docker.client.model.ImageInspectResponse;
import com.kpelykh.docker.client.model.Info;
import com.kpelykh.docker.client.model.SearchItem;

/**
 * Unit test for DockerClient.
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "SimpleServiceTest-context.xml" })
public class DockerClientTest
{
    public static final Logger LOG = LoggerFactory.getLogger(DockerClientTest.class);

    @Autowired
    private DockerClient dockerClient;

    private List<String> tmpImgs = new ArrayList<String>();
    private List<String> tmpContainers = new ArrayList<String>();

    @Before
    public void beforeMethod() throws DockerException {
        LOG.info("Creating image 'busybox'");
        dockerClient.pull("busybox");
    }

    @After
    public void afterMethod() {
    	for (String container : tmpContainers) {
    		LOG.info("Cleaning up temporary container " + container);
    		try {
    			dockerClient.stopContainer(container);
    			dockerClient.removeContainer(container);
    		} catch (DockerException ignore) {}
    	}

    	for (String image : tmpImgs) {
            LOG.info("Cleaning up temporary image " + image);
            try {
                dockerClient.removeImage(image);
            } catch (DockerException ignore) {}
        }
        LOG.info(String.format("################################## END ##################################\n"));
    }

    @Test
    public void shouldFindBusyBoxImage() throws DockerException {
        List<SearchItem> dockerSearch = dockerClient.search("busybox");
        LOG.info("Search returned" + dockerSearch.toString());

        Matcher matcher = hasItem(hasField("name", equalTo("busybox")));
        assertThat(dockerSearch, matcher);

        assertThat(filter(hasField("name", is("busybox")), dockerSearch).size(), equalTo(1));
    }

    /*
     * ###################
     * ## LISTING TESTS ##
     * ###################
     */

    @Test
    public void shouldBeAbleToFindAllImages() throws DockerException {
    	List<Image> images = dockerClient.getImages(true);
    	assertThat(images, notNullValue());
    	LOG.info("Images List: " + images);
    	Info info = dockerClient.info();

    	assertThat(images.size(), equalTo(info.images));
    }

    @Test
    public void shouldBeAbleToFindAndReadImages() throws DockerException {
        List<Image> images = dockerClient.getImages(false);
        assertThat(images, notNullValue());
        LOG.info("Images List: " + images);

        for (Image eachImage : images) {
        	assertThat(eachImage.created, is(greaterThan(0L)) );
//        	assertThat(eachImage.size, is(greaterThan(0L)) );
        	assertThat(eachImage.virtualSize, is(greaterThan(0L)) );
        	assertThat(eachImage.id, not(isEmptyString()));
        	assertThat(eachImage.tag, not(isEmptyString()));
        	assertThat(eachImage.repository, not(isEmptyString()));
		}
    }

    @Test
    public void testListContainers() throws DockerException {
        List<Container> containers = dockerClient.listContainers(true);
        assertThat(containers, notNullValue());
        LOG.info("Container List: " + containers);

        int size = containers.size();

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[]{"echo"});

        ContainerCreateResponse container1 = dockerClient.createContainer(containerConfig);
        assertThat(container1.getId(), not(isEmptyString()));
        dockerClient.startContainer(container1.getId());
        tmpContainers.add(container1.getId());

        List containers2 = dockerClient.listContainers(true);
        assertThat(size + 1, is(equalTo(containers2.size())));
        Matcher matcher = hasItem(hasField("id", startsWith(container1.getId())));
        assertThat(containers2, matcher);

        List<Container> filteredContainers = filter(hasField("id", startsWith(container1.getId())), containers2);
        assertThat(filteredContainers.size(), is(equalTo(1)));

        Container container2 = filteredContainers.get(0);
        assertThat(container2.command, not(isEmptyString()));
        assertThat(container2.image, equalTo("busybox:latest"));
    }


    /*
     * #####################
     * ## CONTAINER TESTS ##
     * #####################
     */

    @Test
    public void shouldBeAbleToCreateNewContainer() throws DockerException {
        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[]{"true"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);

        LOG.info("Created container " + container.toString());

        assertThat(container.getId(), not(isEmptyString()));

        tmpContainers.add(container.getId());
    }

    @Test
    public void shouldBeAbleToStartAndInspectFreshlyCreatedContainer() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[]{"true"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        tmpContainers.add(container.getId());

        dockerClient.startContainer(container.getId());

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(container.getId());
        LOG.info("Container Inspect: " + containerInspectResponse.toString());

        assertThat(containerInspectResponse.config, is(notNullValue()));
        assertThat(containerInspectResponse.id, not(isEmptyString()));

        assertThat(containerInspectResponse.id, startsWith(container.getId()));

        assertThat(containerInspectResponse.image, not(isEmptyString()));
        assertThat(containerInspectResponse.state, is(notNullValue()));

        if (!containerInspectResponse.state.running) {
            assertThat(containerInspectResponse.state.exitCode, is(equalTo(0)));
        }

    }

    @Test
    public void shouldBeAbleToWaitForContainerToExitAndInspectStoppedContainer() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[]{"true"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        tmpContainers.add(container.getId());

        dockerClient.startContainer(container.getId());

        int exitCode = dockerClient.waitContainer(container.getId()).getStatusCode();
        LOG.info("Container exit code: " + exitCode);

        assertThat(exitCode, equalTo(0));

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(container.getId());
        LOG.info("Container Inspect: " + containerInspectResponse.toString());

        assertThat(containerInspectResponse.state.running, is(equalTo(false)));
        assertThat(containerInspectResponse.state.exitCode, is(equalTo(exitCode)));

    }

    @Test
    public void shouldBeAbleToAttachToContainerAndGetLogs() throws DockerException, IOException {

        String snippet = "hello world";

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"/bin/echo", snippet});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        tmpContainers.add(container.getId());

        dockerClient.startContainer(container.getId());
        int exitCode = dockerClient.waitContainer(container.getId()).getStatusCode();

        assertThat(exitCode, equalTo(0));

        InputStream response = dockerClient.logContainer(container.getId());

        String fullLog = IOUtils.toString(response);

        LOG.info("Container log: " + fullLog);
        assertThat(fullLog, containsString(snippet));
    }

    @Test
    public void shouldBeAbleToDetectCreatedTestFile() throws DockerException {
        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"touch", "/test"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        tmpContainers.add(container.getId());

        LOG.info("Created container " + container.toString());
        dockerClient.startContainer(container.getId());
        int exitCode = dockerClient.waitContainer(container.getId()).getStatusCode();

        assertThat(exitCode, equalTo(0));

        List<ChangeLog> filesystemDiff = dockerClient.containterDiff(container.getId());
        LOG.info("Container DIFF: " + filesystemDiff.toString());

        assertThat(filesystemDiff.size(), equalTo(4));
        ChangeLog testChangeLog = selectUnique(filesystemDiff, hasField("path", equalTo("/test")));

        assertThat(testChangeLog, hasField("path", equalTo("/test")));
        assertThat(testChangeLog, hasField("kind", equalTo(1)));
    }

    @Test
    public void testStopContainer() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"sleep", "9999"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container " + container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainer(container.getId());
        tmpContainers.add(container.getId());

        LOG.info("Stopping container " + container.getId());
        dockerClient.stopContainer(container.getId(), 2);

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(container.getId());
        LOG.info("Container Inspect:" + containerInspectResponse.toString());

        assertThat(containerInspectResponse.state.running, is(equalTo(false)));
        assertThat(containerInspectResponse.state.exitCode, not(equalTo(0)));
    }

    @Test
    public void testKillContainer() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"sleep", "9999"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container " + container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainer(container.getId());
        tmpContainers.add(container.getId());

        LOG.info("Killing container " + container.getId());
        dockerClient.kill(container.getId());

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(container.getId());
        LOG.info("Container Inspect:" + containerInspectResponse.toString());

        assertThat(containerInspectResponse.state.running, is(equalTo(false)));
        assertThat(containerInspectResponse.state.exitCode, not(equalTo(0)));

    }

    @Test
    public void restartContainer() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"sleep", "9999"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container " + container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainer(container.getId());
        tmpContainers.add(container.getId());

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(container.getId());
        LOG.info("Container Inspect:" + containerInspectResponse.toString());

        String startTime = containerInspectResponse.state.startedAt;

        dockerClient.restart(container.getId(), 2);

        ContainerInspectResponse containerInspectResponse2 = dockerClient.inspectContainer(container.getId());
        LOG.info("Container Inspect After Restart:" + containerInspectResponse2.toString());

        String startTime2 = containerInspectResponse2.state.startedAt;

        assertThat(startTime, not(equalTo(startTime2)));

        assertThat(containerInspectResponse.state.running, is(equalTo(true)));

        dockerClient.kill(container.getId());
    }

    @Test
    public void removeContainer() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"true"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);

        dockerClient.startContainer(container.getId());
        dockerClient.waitContainer(container.getId());
        tmpContainers.add(container.getId());

        LOG.info("Removing container " + container.getId());
        dockerClient.removeContainer(container.getId());
        tmpContainers.remove(container.getId());

        List containers2 = dockerClient.listContainers(true);
        Matcher matcher = not(hasItem(hasField("id", startsWith(container.getId()))));
        assertThat(containers2, matcher);

    }

    /*
     * ##################
     * ## IMAGES TESTS ##
     * ##################
     * */

    @Test
    public void testPullImage() throws DockerException, IOException {

        String testImage = "joffrey/test001";

        LOG.info("Removing image " + testImage);
        dockerClient.removeImage(testImage);

        Info info = dockerClient.info();
        LOG.info("Client info " + info.toString());

        int imgCount= info.images;

        LOG.info("Pulling image " + testImage);

        dockerClient.pull(testImage);
//        ClientResponse response = dockerClient.pull(testImage);
//
//        StringWriter logwriter = new StringWriter();
//
//        try {
//            LineIterator itr = IOUtils.lineIterator(response.getEntityInputStream(), "UTF-8");
//            while (itr.hasNext()) {
//                String line = itr.next();
//                logwriter.write(line + "\n");
//                LOG.info(line);
//            }
//        } finally {
//            IOUtils.closeQuietly(response.getEntityInputStream());
//        }
//
//        String fullLog = logwriter.toString();
//        assertThat(fullLog, containsString("Pulling repository joffrey/test001"));

        tmpImgs.add(testImage);

        info = dockerClient.info();
        LOG.info("Client info after pull " + info.toString());

        assertThat(imgCount + 2, equalTo(info.images));

        ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(testImage);
        LOG.info("Image Inspect: " + imageInspectResponse.toString());
        assertThat(imageInspectResponse, notNullValue());
    }


    @Test
    public void commitImage() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"touch", "/test"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container " + container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainer(container.getId());
        tmpContainers.add(container.getId());

        LOG.info("Commiting container " + container.toString());
        String imageId = dockerClient.commit(new CommitConfig.Builder(container.getId()).build());
        tmpImgs.add(imageId);

        ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(imageId);
        LOG.info("Image Inspect: " + imageInspectResponse.toString());

        assertThat(imageInspectResponse, hasField("container", startsWith(container.getId())));
        assertThat(imageInspectResponse.containerConfig.getImage(), equalTo("busybox"));

        ImageInspectResponse busyboxImg = dockerClient.inspectImage("busybox");

        assertThat(imageInspectResponse.parent, equalTo(busyboxImg.id));
    }

    @Test
    public void testRemoveImage() throws DockerException {


        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"touch", "/test"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container " + container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainer(container.getId());
        tmpContainers.add(container.getId());


        LOG.info("Commiting container " + container.toString());
        String imageId = dockerClient.commit(new CommitConfig.Builder(container.getId()).build());
        tmpImgs.add(imageId);

        LOG.info("Removing image" + imageId);
        dockerClient.removeImage(imageId);

        List containers = dockerClient.listContainers(true);
        Matcher matcher = not(hasItem(hasField("id", startsWith(imageId))));
        assertThat(containers, matcher);
    }


    /*
     *
     * ################
     * ## MISC TESTS ##
     * ################
     */

    @Test
    public void testRunShlex() throws DockerException {

        String[] commands = new String[] {
                "true",
                "echo \"The Young Descendant of Tepes & Septette for the Dead Princess\"",
                "echo -n 'The Young Descendant of Tepes & Septette for the Dead Princess'",
                "/bin/sh -c echo Hello World",
                "/bin/sh -c echo 'Hello World'",
                "echo 'Night of Nights'",
                "true && echo 'Night of Nights'"
        };

        for (String command : commands) {
            LOG.info("Running command [" + command + "]");

            ContainerConfig containerConfig = new ContainerConfig();
            containerConfig.setImage("busybox");
            containerConfig.setCmd( commands );

            ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
            dockerClient.startContainer(container.getId());
            tmpContainers.add(container.getId());
            int exitcode = dockerClient.waitContainer(container.getId()).getStatusCode();
            assertThat(exitcode, equalTo(0));
        }
    }

    @Test
    public void testNgixDockerfileBuilder() throws DockerException, IOException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("nginx").getFile());

        InputStream response = dockerClient.build(baseDir);

        StringWriter logwriter = new StringWriter();

        try {
            LineIterator itr = IOUtils.lineIterator(response, "UTF-8");
            while (itr.hasNext()) {
                String line = (String) itr.next();
                logwriter.write(line + "\n");
                LOG.info(line);
            }
        } finally {
            IOUtils.closeQuietly(response);
        }

        String fullLog = logwriter.toString();
        assertThat(fullLog, containsString("Successfully built"));

        String imageId = extractImageId(fullLog);

        ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(imageId);
        assertThat(imageInspectResponse, not(nullValue()));
        LOG.info("Image Inspect:" + imageInspectResponse.toString());
        tmpImgs.add(imageInspectResponse.id);

        assertThat(imageInspectResponse.author, equalTo("Guillaume J. Charmes \"guillaume@dotcloud.com\""));
    }

    @Test
    public void testDockerBuilderAddFile() throws DockerException, IOException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("testAddFile").getFile());
        dockerfileBuild(baseDir, "Successfully executed testrun.sh");
    }

    @Test
    public void testDockerBuilderAddFolder() throws DockerException, IOException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("testAddFolder").getFile());
        dockerfileBuild(baseDir, "Successfully executed testAddFolder.sh");
    }

    @Test
    public void testNetCatDockerfileBuilder() throws DockerException, IOException, InterruptedException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("netcat").getFile());

        InputStream response = dockerClient.build(baseDir);

        StringWriter logwriter = new StringWriter();

        try {
            LineIterator itr = IOUtils.lineIterator(response, "UTF-8");
            while (itr.hasNext()) {
                String line = (String) itr.next();
                logwriter.write(line + "\n");
                LOG.info(line);
            }
        } finally {
            IOUtils.closeQuietly(response);
        }

        String fullLog = logwriter.toString();
        assertThat(fullLog, containsString("Successfully built"));

        String imageId = extractImageId(fullLog);

        ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(imageId);
        assertThat(imageInspectResponse, not(nullValue()));
        LOG.info("Image Inspect:" + imageInspectResponse.toString());
        tmpImgs.add(imageInspectResponse.id);

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage(imageInspectResponse.id);
        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        assertThat(container.getId(), not(isEmptyString()));

        HostConfig hostConfig = new HostConfig();
		dockerClient.startContainer(container.getId(), hostConfig );
        tmpContainers.add(container.getId());

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(container.getId());

        assertThat(containerInspectResponse.id, notNullValue());
        assertThat(containerInspectResponse.networkSettings.portMapping, nullValue());
        assertThat(containerInspectResponse.networkSettings.ports, notNullValue());

        assertThat(containerInspectResponse.networkSettings.ports, notNullValue());
        assertTrue(containerInspectResponse.networkSettings.ports.containsKey("6900/tcp"));

        // TODO - default behavior has changed? Ports are *not* exposed any more by default?
//        int port = Integer.valueOf(containerInspectResponse.networkSettings.portMapping.get("Tcp").get("6900"));
//        LOG.info("Checking port {} is open", port);
//        assertThat(available(port), is(false));

        dockerClient.stopContainer(container.getId(), 30);
        dockerClient.waitContainer(container.getId());

//        LOG.info("Checking port {} is closed", port);
//        assertThat(available(port), is(true));
    }

    // UTIL

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean available(int port) {
        if (port < 1100 || port > 60000) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                /* should not be thrown */
                }
            }
        }

        return false;
    }

    private void dockerfileBuild(File baseDir, String expectedText) throws DockerException, IOException {

        //Build image
        InputStream response = dockerClient.build(baseDir);

        StringWriter logwriter = new StringWriter();

        try {
            LineIterator itr = IOUtils.lineIterator(response, "UTF-8");
            while (itr.hasNext()) {
                String line = (String) itr.next();
                logwriter.write(line + "\n");
                LOG.info(line);
            }
        } finally {
            IOUtils.closeQuietly(response);
        }

        String fullLog = logwriter.toString();
        assertThat(fullLog, containsString("Successfully built"));

        String imageId = extractImageId(fullLog);

        //Create container based on image
        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage(imageId);
        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container " + container.toString());
        assertThat(container.getId(), not(isEmptyString()));

        dockerClient.startContainer(container.getId());
        dockerClient.waitContainer(container.getId());

        tmpContainers.add(container.getId());

        //Log container
        InputStream logResponse = dockerClient.logContainer(container.getId());

        StringWriter logwriter2 = new StringWriter();

        try {
            LineIterator itr = IOUtils.lineIterator(logResponse, "UTF-8");
            while (itr.hasNext()) {
                String line = (String) itr.next();
                logwriter2.write(line + (itr.hasNext() ? "\n" : ""));
                LOG.info(line);
            }
        } finally {
            IOUtils.closeQuietly(logResponse);
        }

        assertThat(logwriter2.toString(), containsString(expectedText));
    }

	private String extractImageId(String fullLog) {
		String imageId = StringUtils.substringAfterLast(fullLog, "Successfully built ").trim();
        System.out.println(imageId);
        imageId = org.springframework.util.StringUtils.deleteAny(imageId, "\\n\"}");
        System.out.println(imageId);
		return imageId;
	}
}