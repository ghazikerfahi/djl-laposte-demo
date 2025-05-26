package com.laposte.djl.service;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.convolutional.Conv2d;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.nn.pooling.Pool;
import ai.djl.repository.zoo.*;
import ai.djl.translate.*;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
public class ImageRecognitionService {

    private Model model;

    @PostConstruct
    public void initModel() {
        try {
            SequentialBlock block = new SequentialBlock()
                    // Convolution + BatchNorm + ReLU
                    .add(Conv2d.builder()
                            .setKernelShape(new Shape(3, 3))
                            .optPadding(new Shape(1, 1)) // pour garder la taille
                            .setFilters(16)
                            .build())
                    .add(BatchNorm.builder().build())
                    .add(Activation::relu)

                    // Max pooling pour réduire la taille
                    .add(Pool.maxPool2dBlock(new Shape(2, 2), new Shape(2, 2)))

                    // Flatten pour passer à la couche dense
                    .add(Blocks.batchFlattenBlock())

                    // Couche fully connected finale pour la classification
                    .add(Linear.builder().setUnits(3).build());

            model = Model.newInstance("product-classifier", Device.cpu());
            model.setBlock(block);
            model.load(Paths.get("trained_model"), "product_model");

            System.out.println("Model loaded successfully.");
        } catch (IOException | MalformedModelException e) {
            System.err.println("Failed to load model: " + e.getMessage());
        }
    }

    public String predict(MultipartFile file) throws IOException, TranslateException {
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                .setTypes(Image.class, Classifications.class)
                .optEngine("PyTorch")
                .build();

        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
             Predictor<Image, Classifications> predictor = model.newPredictor()) {

            Image img = ImageFactory.getInstance().fromInputStream(file.getInputStream());
            Classifications result = predictor.predict(img);
            return result.best().toString();
        } catch (ModelNotFoundException e) {
            throw new RuntimeException(e);
        } catch (MalformedModelException e) {
            throw new RuntimeException(e);
        }
    }

    public String predictCustom(MultipartFile file) throws IOException {
        if (model == null) {
            return "Model not loaded";
        }

        Image img = ImageFactory.getInstance().fromInputStream(file.getInputStream());

        try {
            Pipeline pipeline = new Pipeline()
                    .add(new Resize(224, 224))
                    .add(new ToTensor())
                    .add(new Normalize(
                            new float[]{0.485f, 0.456f, 0.406f},
                            new float[]{0.229f, 0.224f, 0.225f}
                    ));

            Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                    .setPipeline(pipeline)
                    .optApplySoftmax(true)
                    .optSynset(Arrays.asList("boite_postale","chronopost","colissimo"))
                    .build();

            try (Predictor<Image, Classifications> predictor = model.newPredictor(translator)) {
                Classifications result = predictor.predict(img);
                return result.best().toString();
            } catch (Exception e) {
                return "Prediction failed: " + e.getMessage();
            }
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }
}