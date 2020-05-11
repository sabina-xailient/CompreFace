package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.system.SystemServiceImpl;
import lombok.val;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@Import({FaceService.class, FaceDao.class, SystemServiceImpl.class})
@MockBeans({@MockBean(RetrainService.class), @MockBean(FaceClassifierManager.class)})
public class FaceServiceITest {

    @Autowired
    private FacesRepository facesRepository;
    @Autowired
    private FaceService faceService;
    private final static String MODEL_KEY = UUID.randomUUID().toString();
    private final static String APP_KEY = UUID.randomUUID().toString();
    private final static String MODEL_KEY_OTHER = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        val faceA = makeFace("A", MODEL_KEY);
        val faceB = makeFace("B", MODEL_KEY_OTHER);
        val faceC = makeFace("C", MODEL_KEY);

        facesRepository.saveAll(List.of(faceA, faceB, faceC));
    }
    @AfterEach
    public void cleanUp() {
        facesRepository.deleteAll();
    }

    public static Face makeFace(final String name, final String modelApiKey) {
        val face = new Face();
        face.setFaceName(name);
        face.setApiKey(modelApiKey);
        face.setEmbeddings(List.of(
                new Face.Embedding()
                        .setEmbedding(List.of(0.0D))
                        .setCalculatorVersion("1.0")
                )
        );
        face.setFaceImgId(new ObjectId("hex-string-1".getBytes()));
        face.setRawImgId(new ObjectId("hex-string-2".getBytes()));
        face.setId("Id_" + name);

        return face;
    }


    @Test
    public void updateModelKeySuccess() {
        val newModelKey = UUID.randomUUID().toString();
        assertThat(facesRepository.findByApiKey(MODEL_KEY)).hasSize(2);
        assertThat(facesRepository.findByApiKey(newModelKey)).hasSize(0);

        faceService.updateModelApiKeyForFaces(APP_KEY + MODEL_KEY, newModelKey);

        assertThat(facesRepository.findByApiKey(MODEL_KEY)).hasSize(0);
        assertThat(facesRepository.findByApiKey(newModelKey)).hasSize(2);
    }
}