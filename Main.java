import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class Main {

    // ============================================================
    // GLOBAL CONSTANTS
    // ============================================================

    static final double PI = Math.PI;

    // ============================================================
    // COMPLEX NUMBER CLASS
    // ============================================================

    static class Complex {

        double real;
        double imag;

        Complex(double real, double imag) {
            this.real = real;
            this.imag = imag;
        }

        Complex add(Complex other) {
            return new Complex(real + other.real, imag + other.imag);
        }

        Complex subtract(Complex other) {
            return new Complex(real - other.real, imag - other.imag);
        }

        Complex multiply(Complex other) {
            return new Complex(real * other.real - imag * other.imag, real * other.imag + imag * other.real);
        }

        Complex scale(double value) {
            return new Complex(real * value, imag * value);
        }

        Complex conjugate() {
            return new Complex(real, -imag);
        }

        double magnitude() {
            return Math.sqrt(real * real + imag * imag);
        }

        double phase() {
            return Math.atan2(imag, real);
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%.6f %+.6fi", real, imag);
        }
    }

    // ============================================================
    // SIGNAL GENERATOR
    // ============================================================

    static double sine(double time, double frequency, double amplitude, double phase) {

        return amplitude * Math.sin(2.0 * PI * frequency * time + phase);
    }

    static double cosine(double time, double frequency, double amplitude, double phase) {

        return amplitude * Math.cos(2.0 * PI * frequency * time + phase);
    }

    // ============================================================
    // COMPLEX SIGNAL
    // ============================================================

    static double createCompositeSignal(double time) {

        double signal = 0;

        signal += sine(time, 50, 1.0, 0);

        signal += sine(time, 120, 0.65, PI / 4);

        signal += sine(time, 300, 0.35, PI / 7);

        signal += sine(time, 750, 0.20, PI / 3);

        signal += 0.15 * cosine(time, 1500, 1, PI / 6);

        return signal;
    }

    // ============================================================
    // GAUSSIAN NOISE
    // ============================================================

    static void addGaussianNoise(double[] signal, double standardDeviation, Random random) {

        for (int i = 0; i < signal.length; i++) {

            double noise = random.nextGaussian() * standardDeviation;

            signal[i] += noise;
        }
    }

    // ============================================================
    // NUMERICAL DIFFERENTIATION
    // ============================================================

    static double[] differentiate(double[] signal, double dt) {

        double[] derivative = new double[signal.length];

        derivative[0] = (signal[1] - signal[0]) / dt;

        for (int i = 1; i < signal.length - 1; i++) {

            derivative[i] = (signal[i + 1] - signal[i - 1]) / (2.0 * dt);
        }

        int last = signal.length - 1;

        derivative[last] = (signal[last] - signal[last - 1]) / dt;

        return derivative;
    }

    // ============================================================
    // NUMERICAL INTEGRATION
    // TRAPEZOIDAL CUMULATIVE INTEGRAL
    // ============================================================

    static double[] cumulativeIntegral(double[] signal, double dt) {

        double[] integral = new double[signal.length];

        integral[0] = 0;

        for (int i = 1; i < signal.length; i++) {

            integral[i] = integral[i - 1] + 0.5 * (signal[i] + signal[i - 1]) * dt;
        }

        return integral;
    }

    // ============================================================
    // SIMPSON'S 1/3 NUMERICAL INTEGRATION
    // ============================================================

    static double simpsonIntegral(double[] signal, double dt) {

        int n = signal.length - 1;

        if (n % 2 != 0) {
            n--;
        }

        double result = signal[0] + signal[n];

        for (int i = 1; i < n; i++) {

            if (i % 2 == 0) {
                result += 2.0 * signal[i];
            } else {
                result += 4.0 * signal[i];
            }
        }

        return result * dt / 3.0;
    }

    // ============================================================
    // DISCRETE CONVOLUTION
    // ============================================================

    static double[] convolution(double[] x, double[] h) {

        int outputLength = x.length + h.length - 1;

        double[] y = new double[outputLength];

        for (int n = 0; n < outputLength; n++) {

            double sum = 0;

            int kStart = Math.max(0, n - h.length + 1);

            int kEnd = Math.min(n, x.length - 1);

            for (int k = kStart; k <= kEnd; k++) {

                int hIndex = n - k;

                sum += x[k] * h[hIndex];
            }

            y[n] = sum;
        }

        return y;
    }

    // ============================================================
    // MOVING AVERAGE FIR FILTER
    // ============================================================

    static double[] movingAverageFilter(double[] signal, int windowSize) {

        double[] output = new double[signal.length];

        double sum = 0;

        for (int i = 0; i < signal.length; i++) {

            sum += signal[i];

            if (i >= windowSize) {
                sum -= signal[i - windowSize];
            }

            int divisor = Math.min(i + 1, windowSize);

            output[i] = sum / divisor;
        }

        return output;
    }

    // ============================================================
    // FIRST ORDER IIR LOW PASS FILTER
    // ============================================================

    static double[] iirLowPass(double[] input, double sampleRate, double cutoffFrequency) {

        double[] output = new double[input.length];

        double dt = 1.0 / sampleRate;

        double RC = 1.0 / (2.0 * PI * cutoffFrequency);

        double alpha = dt / (RC + dt);

        output[0] = input[0];

        for (int i = 1; i < input.length; i++) {

            output[i] = output[i - 1] + alpha * (input[i] - output[i - 1]);
        }

        return output;
    }

    // ============================================================
    // CORRELATION
    // ============================================================

    static double correlation(double[] x, double[] y) {

        int length = Math.min(x.length, y.length);

        double meanX = 0;
        double meanY = 0;

        for (int i = 0; i < length; i++) {

            meanX += x[i];
            meanY += y[i];
        }

        meanX /= length;
        meanY /= length;

        double numerator = 0;
        double energyX = 0;
        double energyY = 0;

        for (int i = 0; i < length; i++) {

            double dx = x[i] - meanX;

            double dy = y[i] - meanY;

            numerator += dx * dy;

            energyX += dx * dx;

            energyY += dy * dy;
        }

        if (energyX == 0 || energyY == 0) {

            return 0;
        }

        return numerator / Math.sqrt(energyX * energyY);
    }

    // ============================================================
    // AUTOCORRELATION
    // ============================================================

    static double[] autocorrelation(double[] signal, int maximumLag) {

        double[] result = new double[maximumLag + 1];

        for (int lag = 0; lag <= maximumLag; lag++) {

            double sum = 0;

            for (int i = 0; i < signal.length - lag; i++) {

                sum += signal[i] * signal[i + lag];
            }

            result[lag] = sum / (signal.length - lag);
        }

        return result;
    }

    // ============================================================
    // ENERGY
    // ============================================================

    static double energy(double[] signal) {

        double total = 0;

        for (double value : signal) {

            total += value * value;
        }

        return total;
    }

    // ============================================================
    // RMS
    // ============================================================

    static double rms(double[] signal) {

        return Math.sqrt(energy(signal) / signal.length);
    }

    // ============================================================
    // SIGNAL TO NOISE RATIO
    // ============================================================

    static double calculateSNR(double[] clean, double[] noisy) {

        double signalPower = 0;
        double noisePower = 0;

        int length = Math.min(clean.length, noisy.length);

        for (int i = 0; i < length; i++) {

            double signal = clean[i];

            double noise = noisy[i] - clean[i];

            signalPower += signal * signal;

            noisePower += noise * noise;
        }

        signalPower /= length;
        noisePower /= length;

        return 10.0 * Math.log10(signalPower / noisePower);
    }

    // ============================================================
    // FFT
    // ============================================================

    static void fft(Complex[] data, boolean inverse) {

        int n = data.length;

        // Bit reversal

        for (int i = 1, j = 0; i < n; i++) {

            int bit = n >> 1;

            for (; (j & bit) != 0; bit >>= 1) {

                j ^= bit;
            }

            j ^= bit;

            if (i < j) {

                Complex temp = data[i];

                data[i] = data[j];

                data[j] = temp;
            }
        }

        // FFT stages

        for (int length = 2; length <= n; length <<= 1) {

            double angle = 2.0 * PI / length;

            if (!inverse) {
                angle = -angle;
            }

            Complex wLength = new Complex(Math.cos(angle), Math.sin(angle));

            for (int i = 0; i < n; i += length) {

                Complex w = new Complex(1, 0);

                for (int j = 0; j < length / 2; j++) {

                    Complex even = data[i + j];

                    Complex odd = data[i + j + length / 2];

                    Complex multiplied = odd.multiply(w);

                    data[i + j] = even.add(multiplied);

                    data[i + j + length / 2] = even.subtract(multiplied);

                    w = w.multiply(wLength);
                }
            }
        }

        // Normalize inverse FFT

        if (inverse) {

            for (int i = 0; i < n; i++) {

                data[i] = data[i].scale(1.0 / n);
            }
        }
    }

    // ============================================================
    // CREATE FFT INPUT
    // ============================================================

    static Complex[] realToComplex(double[] signal, int fftSize) {

        Complex[] result = new Complex[fftSize];

        for (int i = 0; i < fftSize; i++) {

            if (i < signal.length) {

                result[i] = new Complex(signal[i], 0);

            } else {

                result[i] = new Complex(0, 0);
            }
        }

        return result;
    }

    // ============================================================
    // PRINT MAJOR FREQUENCIES
    // ============================================================

    static void analyzeSpectrum(Complex[] spectrum, double sampleRate) {

        int n = spectrum.length;

        double[] magnitude = new double[n / 2];

        for (int i = 0; i < n / 2; i++) {

            magnitude[i] = spectrum[i].magnitude() / n;
        }

        System.out.println();

        System.out.println("Top frequency components:");

        double[] copy = Arrays.copyOf(magnitude, magnitude.length);

        for (int peak = 0; peak < 10; peak++) {

            int index = 0;

            for (int i = 1; i < copy.length; i++) {

                if (copy[i] > copy[index]) {
                    index = i;
                }
            }

            double frequency = index * sampleRate / n;

            System.out.printf(Locale.US, "%2d. Frequency = %8.2f Hz | Magnitude = %.6f%n", peak + 1, frequency,
                    copy[index]);

            copy[index] = -1;
        }
    }

    // ============================================================
    // RK4 DIFFERENTIAL EQUATION SOLVER
    //
    // SECOND ORDER SYSTEM:
    //
    // x'' + 2*zeta*omega*x' + omega^2*x = input
    //
    // Converted to:
    //
    // x' = v
    //
    // v' = input
    // - 2*zeta*omega*v
    // - omega^2*x
    // ============================================================

    static double[] simulateSecondOrderSystem(double[] input, double dt, double naturalFrequency, double dampingRatio) {

        double[] output = new double[input.length];

        double x = 0;
        double v = 0;

        double omega = 2.0 * PI * naturalFrequency;

        for (int i = 0; i < input.length; i++) {

            double force = input[i];

            double k1x = v;

            double k1v = force - 2.0 * dampingRatio * omega * v - omega * omega * x;

            double x2 = x + 0.5 * dt * k1x;

            double v2 = v + 0.5 * dt * k1v;

            double k2x = v2;

            double k2v = force - 2.0 * dampingRatio * omega * v2 - omega * omega * x2;

            double x3 = x + 0.5 * dt * k2x;

            double v3 = v + 0.5 * dt * k2v;

            double k3x = v3;

            double k3v = force - 2.0 * dampingRatio * omega * v3 - omega * omega * x3;

            double x4 = x + dt * k3x;

            double v4 = v + dt * k3v;

            double k4x = v4;

            double k4v = force - 2.0 * dampingRatio * omega * v4 - omega * omega * x4;

            x += dt / 6.0 * (k1x + 2 * k2x + 2 * k3x + k4x);

            v += dt / 6.0 * (k1v + 2 * k2v + 2 * k3v + k4v);

            output[i] = x;
        }

        return output;
    }

    // ============================================================
    // STEP RESPONSE
    // ============================================================

    static double[] createStepSignal(int length, double amplitude) {

        double[] step = new double[length];

        Arrays.fill(step, amplitude);

        return step;
    }

    // ============================================================
    // IMPULSE SIGNAL
    // ============================================================

    static double[] createImpulse(int length) {

        double[] impulse = new double[length];

        impulse[0] = 1.0;

        return impulse;
    }

    // ============================================================
    // DISPLAY SIGNAL STATISTICS
    // ============================================================

    static void displayStatistics(String name, double[] signal) {

        double minimum = signal[0];

        double maximum = signal[0];

        double sum = 0;

        for (double value : signal) {

            minimum = Math.min(minimum, value);

            maximum = Math.max(maximum, value);

            sum += value;
        }

        double mean = sum / signal.length;

        System.out.println();

        System.out.println("========== " + name + " ==========");

        System.out.println("Samples : " + signal.length);

        System.out.printf(Locale.US, "Minimum : %.8f%n", minimum);

        System.out.printf(Locale.US, "Maximum : %.8f%n", maximum);

        System.out.printf(Locale.US, "Mean    : %.8f%n", mean);

        System.out.printf(Locale.US, "RMS     : %.8f%n", rms(signal));

        System.out.printf(Locale.US, "Energy  : %.8f%n", energy(signal));

        System.out.println("================================");
    }

    // ============================================================
    // MAIN
    // ============================================================

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);

        long programStart = System.nanoTime();

        System.out.println();

        System.out.println("============================================================");

        System.out.println("        JAVA DIGITAL SIGNAL PROCESSING LAB");

        System.out.println("        SIGNALS & SYSTEMS / DSP SIMULATION");

        System.out.println("============================================================");

        System.out.println();

        System.out.println("Java Version : " + System.getProperty("java.version"));

        System.out.println("Processors   : " + Runtime.getRuntime().availableProcessors());

        System.out.println();

        // ========================================================
        // SAMPLING PARAMETERS
        // ========================================================

        double sampleRate = 8000.0;

        double duration = 8.0;

        int numberOfSamples = (int) (sampleRate * duration);

        double dt = 1.0 / sampleRate;

        System.out.println("Sample Rate      : " + sampleRate + " Hz");

        System.out.println("Duration         : " + duration + " seconds");

        System.out.println("Number of samples: " + numberOfSamples);

        System.out.println();

        // ========================================================
        // GENERATE CLEAN SIGNAL
        // ========================================================

        System.out.println("Generating composite signal...");

        double[] cleanSignal = new double[numberOfSamples];

        double[] time = new double[numberOfSamples];

        for (int i = 0; i < numberOfSamples; i++) {

            time[i] = i * dt;

            cleanSignal[i] = createCompositeSignal(time[i]);
        }

        displayStatistics("CLEAN SIGNAL", cleanSignal);

        // ========================================================
        // ADD NOISE
        // ========================================================

        System.out.println();

        System.out.println("Adding Gaussian noise...");

        double[] noisySignal = Arrays.copyOf(cleanSignal, cleanSignal.length);

        Random random = new Random(20261004);

        addGaussianNoise(noisySignal, 0.45, random);

        displayStatistics("NOISY SIGNAL", noisySignal);

        double snr = calculateSNR(cleanSignal, noisySignal);

        System.out.printf(Locale.US, "%nInitial SNR = %.4f dB%n", snr);

        // ========================================================
        // DIFFERENTIATION
        // ========================================================

        System.out.println();

        System.out.println("Performing numerical differentiation...");

        long differentiationStart = System.nanoTime();

        double[] derivative = differentiate(noisySignal, dt);

        long differentiationEnd = System.nanoTime();

        System.out.printf(Locale.US, "Differentiation time: %.4f seconds%n",
                (differentiationEnd - differentiationStart) / 1_000_000_000.0);

        displayStatistics("DERIVATIVE", derivative);

        // ========================================================
        // NUMERICAL INTEGRATION
        // ========================================================

        System.out.println();

        System.out.println("Performing numerical integration...");

        long integrationStart = System.nanoTime();

        double[] integral = cumulativeIntegral(noisySignal, dt);

        long integrationEnd = System.nanoTime();

        System.out.printf(Locale.US, "Integration time: %.4f seconds%n",
                (integrationEnd - integrationStart) / 1_000_000_000.0);

        displayStatistics("INTEGRATED SIGNAL", integral);

        // ========================================================
        // SIMPSON INTEGRATION
        // ========================================================

        System.out.println();

        System.out.println("Applying Simpson's 1/3 integration...");

        double totalIntegral = simpsonIntegral(noisySignal, dt);

        System.out.printf(Locale.US, "Total Simpson integral = %.10f%n", totalIntegral);

        // ========================================================
        // FIR FILTER
        // ========================================================

        System.out.println();

        System.out.println("Applying FIR moving-average filter...");

        long firStart = System.nanoTime();

        double[] firFiltered = movingAverageFilter(noisySignal, 31);

        long firEnd = System.nanoTime();

        System.out.printf(Locale.US, "FIR filtering time: %.4f seconds%n", (firEnd - firStart) / 1_000_000_000.0);

        double filteredSNR = calculateSNR(cleanSignal, firFiltered);

        System.out.printf(Locale.US, "FIR filtered SNR = %.4f dB%n", filteredSNR);

        // ========================================================
        // IIR FILTER
        // ========================================================

        System.out.println();

        System.out.println("Applying first-order IIR low-pass filter...");

        long iirStart = System.nanoTime();

        double[] iirFiltered = iirLowPass(noisySignal, sampleRate, 500);

        long iirEnd = System.nanoTime();

        System.out.printf(Locale.US, "IIR filtering time: %.4f seconds%n", (iirEnd - iirStart) / 1_000_000_000.0);

        double iirSNR = calculateSNR(cleanSignal, iirFiltered);

        System.out.printf(Locale.US, "IIR filtered SNR = %.4f dB%n", iirSNR);

        // ========================================================
        // CONVOLUTION
        // ========================================================

        System.out.println();

        System.out.println("Performing discrete convolution...");

        double[] impulseResponse = {

                0.02, 0.04, 0.06, 0.08, 0.10, 0.12, 0.14, 0.12, 0.10, 0.08, 0.06, 0.04, 0.02 };

        long convolutionStart = System.nanoTime();

        double[] convolutionResult = convolution(noisySignal, impulseResponse);

        long convolutionEnd = System.nanoTime();

        System.out.printf(Locale.US, "Convolution time: %.4f seconds%n",
                (convolutionEnd - convolutionStart) / 1_000_000_000.0);

        System.out.println("Convolution output samples: " + convolutionResult.length);

        // ========================================================
        // AUTOCORRELATION
        // ========================================================

        System.out.println();

        System.out.println("Calculating autocorrelation...");

        long autocorrelationStart = System.nanoTime();

        double[] autocorrelation = autocorrelation(noisySignal, 5000);

        long autocorrelationEnd = System.nanoTime();

        System.out.printf(Locale.US, "Autocorrelation time: %.4f seconds%n",
                (autocorrelationEnd - autocorrelationStart) / 1_000_000_000.0);

        System.out.printf(Locale.US, "Autocorrelation R[0] = %.8f%n", autocorrelation[0]);

        // ========================================================
        // CROSS CORRELATION
        // ========================================================

        System.out.println();

        double correlationValue = correlation(cleanSignal, noisySignal);

        System.out.printf(Locale.US, "Clean/noisy correlation = %.8f%n", correlationValue);

        // ========================================================
        // FFT
        // ========================================================

        System.out.println();

        System.out.println("============================================================");

        System.out.println("                FAST FOURIER TRANSFORM");

        System.out.println("============================================================");

        int fftSize = 65536;

        System.out.println("FFT size = " + fftSize);

        Complex[] fftInput = realToComplex(noisySignal, fftSize);

        long fftStart = System.nanoTime();

        fft(fftInput, false);

        long fftEnd = System.nanoTime();

        double fftTime = (fftEnd - fftStart) / 1_000_000_000.0;

        System.out.printf(Locale.US, "FFT execution time = %.6f seconds%n", fftTime);

        analyzeSpectrum(fftInput, sampleRate);

        // ========================================================
        // INVERSE FFT
        // ========================================================

        System.out.println();

        System.out.println("Performing inverse FFT...");

        long inverseStart = System.nanoTime();

        fft(fftInput, true);

        long inverseEnd = System.nanoTime();

        System.out.printf(Locale.US, "Inverse FFT time = %.6f seconds%n",
                (inverseEnd - inverseStart) / 1_000_000_000.0);

        double reconstructionError = 0;

        for (int i = 0; i < cleanSignal.length; i++) {

            double error = Math.abs(fftInput[i].real - noisySignal[i]);

            reconstructionError = Math.max(reconstructionError, error);
        }

        System.out.printf(Locale.US, "Maximum FFT reconstruction error = %.12f%n", reconstructionError);

        // ========================================================
        // DIFFERENTIAL SYSTEM
        // ========================================================

        System.out.println();

        System.out.println("============================================================");

        System.out.println("          SECOND-ORDER DIFFERENTIAL SYSTEM");

        System.out.println("============================================================");

        double naturalFrequency = 100;

        double dampingRatio = 0.35;

        System.out.println("Natural frequency : " + naturalFrequency + " Hz");

        System.out.println("Damping ratio     : " + dampingRatio);

        long differentialStart = System.nanoTime();

        double[] systemOutput = simulateSecondOrderSystem(cleanSignal, dt, naturalFrequency, dampingRatio);

        long differentialEnd = System.nanoTime();

        System.out.printf(Locale.US, "Differential equation simulation time = %.4f seconds%n",
                (differentialEnd - differentialStart) / 1_000_000_000.0);

        displayStatistics("SYSTEM OUTPUT", systemOutput);

        // ========================================================
        // STEP RESPONSE
        // ========================================================

        System.out.println();

        System.out.println("Calculating system step response...");

        double[] stepInput = createStepSignal(40000, 1.0);

        double[] stepResponse = simulateSecondOrderSystem(stepInput, dt, 100, 0.35);

        System.out.printf(Locale.US, "Step response final value = %.8f%n", stepResponse[stepResponse.length - 1]);

        // ========================================================
        // IMPULSE RESPONSE
        // ========================================================

        System.out.println();

        System.out.println("Calculating system impulse response...");

        double[] impulse = createImpulse(40000);

        double[] impulseResponseSystem = simulateSecondOrderSystem(impulse, dt, 100, 0.35);

        double impulseEnergy = energy(impulseResponseSystem);

        System.out.printf(Locale.US, "Impulse response energy = %.10f%n", impulseEnergy);

        // ========================================================
        // FINAL SUMMARY
        // ========================================================

        long programEnd = System.nanoTime();

        double totalTime = (programEnd - programStart) / 1_000_000_000.0;

        System.out.println();

        System.out.println("============================================================");

        System.out.println("                    FINAL RESULTS");

        System.out.println("============================================================");

        System.out.printf(Locale.US, "Initial SNR           : %.4f dB%n", snr);

        System.out.printf(Locale.US, "FIR filtered SNR      : %.4f dB%n", filteredSNR);

        System.out.printf(Locale.US, "IIR filtered SNR      : %.4f dB%n", iirSNR);

        System.out.printf(Locale.US, "FFT execution         : %.6f seconds%n", fftTime);

        System.out.printf(Locale.US, "Maximum FFT error     : %.12f%n", reconstructionError);

        System.out.printf(Locale.US, "Clean/noisy correlation: %.8f%n", correlationValue);

        System.out.printf(Locale.US, "Total program time    : %.3f seconds%n", totalTime);

        System.out.println();

        System.out.println("Signal generation      [DONE]");

        System.out.println("Numerical differentiation [DONE]");

        System.out.println("Numerical integration  [DONE]");

        System.out.println("Simpson integration    [DONE]");

        System.out.println("FIR filtering          [DONE]");

        System.out.println("IIR filtering          [DONE]");

        System.out.println("Convolution            [DONE]");

        System.out.println("Correlation            [DONE]");

        System.out.println("Autocorrelation        [DONE]");

        System.out.println("FFT                    [DONE]");

        System.out.println("Inverse FFT            [DONE]");

        System.out.println("Differential equation  [DONE]");

        System.out.println("Step response          [DONE]");

        System.out.println("Impulse response       [DONE]");

        System.out.println();

        System.out.println("============================================================");

        System.out.println("             DSP SIMULATION COMPLETED");

        System.out.println("============================================================");

        System.out.println();
    }
}