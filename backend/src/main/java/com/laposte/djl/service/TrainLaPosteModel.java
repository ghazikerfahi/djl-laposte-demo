package com.laposte.djl.service;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.convolutional.Conv2d;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.nn.pooling.Pool;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TrainLaPosteModel {

    public static void main(String[] args) throws IOException, TranslateException {
        ImageFolder dataset = ImageFolder.builder()
                .setRepositoryPath(Paths.get("src/main/resources/dataset"))
                .optMaxDepth(1)
                .addTransform(new Resize(224, 224))
                .addTransform(new ToTensor())
                .setSampling(32, true)
                .build();
        dataset.prepare();

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
                .add(Linear.builder().setUnits(dataset.getSynset().size()).build());

        try (Model model = Model.newInstance("product-classifier", Device.cpu())) {
            model.setBlock(block);

            DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                    .addEvaluator(new Accuracy())
                    .addTrainingListeners(TrainingListener.Defaults.logging());

            Trainer trainer = model.newTrainer(config);
            trainer.setMetrics(new Metrics());
            trainer.initialize(new Shape(1, 3, 224, 224));

            EasyTrain.fit(trainer, 100, dataset, null);

            model.setProperty("application", "peoduct-classifier");
            model.setProperty("input_shape", "1,3,224,224");
            model.setProperty("classes", String.join(",", dataset.getSynset()));
            model.save(Paths.get("trained_model"), "product_model");
            Files.write(Paths.get("trained_model/synset.txt"), String.join("\n", dataset.getSynset()).getBytes());
        }
    }
}