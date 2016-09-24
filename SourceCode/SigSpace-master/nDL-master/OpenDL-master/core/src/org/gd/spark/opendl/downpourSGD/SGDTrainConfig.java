/*
 * Copyright 2013 GuoDing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gd.spark.opendl.downpourSGD;

import java.io.Serializable;

import org.apache.spark.storage.StorageLevel;

import lombok.Data;

/**
 * DownpourSGD train work configuration parameter <p/>
 * 
 * @author GuoDing
 * @since 2013-07-15
 */
@Data
public class SGDTrainConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * number of model replica
     */
    public int nbrModelReplica = 1;
    
    /**
     * specify the spark persistent level for split train sample data
     */
    public StorageLevel mrDataStorage = StorageLevel.MEMORY_ONLY();

    /**
     * stop condition
     */
    public int maxEpochs = 500;

    public int getMaxEpochs() {
        return maxEpochs;
    }

    public double getMinLoss() {
        return minLoss;
    }

    public int getLossCalStep() {
        return lossCalStep;
    }

    public boolean isPrintLoss() {
        return printLoss;
    }

    public boolean isParamOutput() {
        return paramOutput;
    }

    public int getParamOutputStep() {
        return paramOutputStep;
    }

    public String getParamOutputPath() {
        return paramOutputPath;
    }

    public boolean isUseCG() {
        return useCG;
    }

    public int getCgEpochStep() {
        return cgEpochStep;
    }

    public double minLoss = 0.1;

    /**
     * learning rate
     */
    public double learningRate = 0.001; // only for fix lr, or adagrad basic lr

    /**
     * regulation
     */
    public boolean useRegularization = false;
    public double lamada1 = 0; // most case, only use L2 reg is ok
    public double lamada2 = 0.0001;

    /**
     * caculate loss function
     */
    public int lossCalStep = 1; //calculate and print total loss every calStep
    public boolean printLoss = false;
    
    /**
     * parameter output during each epoche of training
     */
    public boolean paramOutput = false;
    public int paramOutputStep = 5; //parameter output every paramOutputStep
    public String paramOutputPath = null;
    
    /**
     * for AutoEncoder sparsity
     */
    public boolean forceSparsity = false;
    public double sparsity = 0.05;
    public double sparsityBeta = 0.01;

    /**
     * for AutoEncoder denoising
     */
    public boolean doCorruption = true;
    public double corruption_level = 0.3;
    
    /**
     * for RBM fast CD1 algorithm by Hinton
     */
    public boolean useHintonCD1 = true;

    /**
     * use ConjugateGradient on front step
     */
    // whether use cg on front step
    public boolean useCG = false;

    // stop use cg(switch to asynchronous SGD) after cgEpochStep epoch times.
    // default is 1, means only use cg first time(coverage fast to low loss).
    public int cgEpochStep = 1;

    // max iteration time in one time cg
    public int cgMaxIterations = 100;

    // the cg min gradient update tolerance
    public double cgTolerance = 0.0001;

    // cg init step size
    public double cgInitStepSize = 0.01;

    public boolean isDoCorruption() {
        return doCorruption;
    }

    public double getCorruption_level() {
        return corruption_level;
    }

    public boolean isUseRegularization() {
        return useRegularization;
    }

    public double getLamada2() {
        return lamada2;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getCgInitStepSize() {
        return cgInitStepSize;
    }

    public double getCgTolerance() {
        return cgTolerance;
    }

    public int getCgMaxIterations() {
        return cgMaxIterations;
    }

    public boolean isUseHintonCD1() {
        return useHintonCD1;
    }


    public double getLamada1() {
        return lamada1;
    }

    public boolean isForceSparsity() {
        return forceSparsity;
    }

    public double getSparsity() {
        return sparsity;
    }

    public double getSparsityBeta() {
        return sparsityBeta;
    }

    public int getNbrModelReplica() {
        return nbrModelReplica;
    }

    public void setNbrModelReplica(int nbrModelReplica) {
        this.nbrModelReplica = nbrModelReplica;
    }

    public void setMrDataStorage(StorageLevel mrDataStorage) {
        this.mrDataStorage = mrDataStorage;
    }

    public void setMaxEpochs(int maxEpochs) {
        this.maxEpochs = maxEpochs;
    }

    public void setMinLoss(double minLoss) {
        this.minLoss = minLoss;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public void setUseRegularization(boolean useRegularization) {
        this.useRegularization = useRegularization;
    }

    public void setLamada1(double lamada1) {
        this.lamada1 = lamada1;
    }

    public void setLamada2(double lamada2) {
        this.lamada2 = lamada2;
    }

    public void setLossCalStep(int lossCalStep) {
        this.lossCalStep = lossCalStep;
    }

    public void setPrintLoss(boolean printLoss) {
        this.printLoss = printLoss;
    }

    public void setParamOutput(boolean paramOutput) {
        this.paramOutput = paramOutput;
    }

    public void setParamOutputStep(int paramOutputStep) {
        this.paramOutputStep = paramOutputStep;
    }

    public void setParamOutputPath(String paramOutputPath) {
        this.paramOutputPath = paramOutputPath;
    }

    public void setForceSparsity(boolean forceSparsity) {
        this.forceSparsity = forceSparsity;
    }

    public void setSparsity(double sparsity) {
        this.sparsity = sparsity;
    }

    public void setSparsityBeta(double sparsityBeta) {
        this.sparsityBeta = sparsityBeta;
    }

    public void setDoCorruption(boolean doCorruption) {
        this.doCorruption = doCorruption;
    }

    public void setCorruption_level(double corruption_level) {
        this.corruption_level = corruption_level;
    }

    public void setUseHintonCD1(boolean useHintonCD1) {
        this.useHintonCD1 = useHintonCD1;
    }

    public void setCgEpochStep(int cgEpochStep) {
        this.cgEpochStep = cgEpochStep;
    }

    public void setCgMaxIterations(int cgMaxIterations) {
        this.cgMaxIterations = cgMaxIterations;
    }

    public void setCgTolerance(double cgTolerance) {
        this.cgTolerance = cgTolerance;
    }

    public void setCgInitStepSize(double cgInitStepSize) {
        this.cgInitStepSize = cgInitStepSize;
    }

    public StorageLevel getMrDataStorage() {

        return mrDataStorage;
    }

    public void setUseCG(boolean useCG) {
        this.useCG = useCG;
    }
}
