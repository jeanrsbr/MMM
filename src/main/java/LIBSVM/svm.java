package MMM.SVM.LIBSVM;

import java.io.*;
import java.util.*;

//
// Kernel Cache
//
// l is the number of total data items
// size is the cache size limit in bytes
//
class Cache {

    private final int l;
    private long size;

    private final class head_t {

        head_t prev, next;	// a cicular list
        float[] data;
        int len;		// data[0,len) is cached in this entry
    }
    private final head_t[] head;
    private head_t lru_head;

    Cache(int l_, long size_) {
        l = l_;
        size = size_;
        head = new head_t[l];
        for (int i = 0; i < l; i++) {
            head[i] = new head_t();
        }
        size /= 4;
        size -= l * (16 / 4);	// sizeof(head_t) == 16
        size = Math.max(size, 2 * (long) l);  // cache must be large enough for two columns
        lru_head = new head_t();
        lru_head.next = lru_head.prev = lru_head;
    }

    private void lru_delete(head_t h) {
        // delete from current location
        h.prev.next = h.next;
        h.next.prev = h.prev;
    }

    private void lru_insert(head_t h) {
        // insert to last position
        h.next = lru_head;
        h.prev = lru_head.prev;
        h.prev.next = h;
        h.next.prev = h;
    }

    // request data [0,len)
    // return some position p where [p,len) need to be filled
    // (p >= len if nothing needs to be filled)
    // java: simulate pointer using single-element array
    int get_data(int index, float[][] data, int len) {
        head_t h = head[index];
        if (h.len > 0) {
            lru_delete(h);
        }
        int more = len - h.len;

        if (more > 0) {
            // free old space
            while (size < more) {
                head_t old = lru_head.next;
                lru_delete(old);
                size += old.len;
                old.data = null;
                old.len = 0;
            }

            // allocate new space
            float[] new_data = new float[len];
            if (h.data != null) {
                System.arraycopy(h.data, 0, new_data, 0, h.len);
            }
            h.data = new_data;
            size -= more;
            do {
                int aux = h.len;
                h.len = len;
                len = aux;
            } while (false);
        }

        lru_insert(h);
        data[0] = h.data;
        return len;
    }

}

//
// Kernel evaluation
//
// the static method k_function is for doing single kernel evaluation
// the constructor of Kernel prepares to calculate the l*l kernel matrix
// the member function get_Q is for getting one column from the Q Matrix
//
abstract class QMatrix {

    abstract float[] get_Q(int column, int len);

    abstract double[] get_QD();

    abstract void swap_index(int i, int j);
};

abstract class Kernel extends QMatrix {

    private svm_node[][] x;
    private final double[] x_square;

    // svm_parameter
    private final int kernel_type;
    private final int degree;
    private final double gamma;
    private final double coef0;

    abstract float[] get_Q(int column, int len);

    abstract double[] get_QD();

    //OK
    void swap_index(int i, int j) {
        do {
            svm_node[] y = x[i];
            x[i] = x[j];
            x[j] = y;
        } while (false);
        if (x_square != null) {
            do {
                double y = x_square[i];
                x_square[i] = x_square[j];
                x_square[j] = y;
            } while (false);
        }
    }

    //OK
    private static double powi(double base, int times) {
        double tmp = base, ret = 1.0;

        for (int t = times; t > 0; t /= 2) {
            if (t % 2 == 1) {
                ret *= tmp;
            }
            tmp = tmp * tmp;
        }
        return ret;
    }

    //OK
    double kernel_function(int i, int j) {
        switch (kernel_type) {
            case svm_parameter.LINEAR:
                return dot(x[i], x[j]);
            case svm_parameter.POLY:
                return powi(gamma * dot(x[i], x[j]) + coef0, degree);
            case svm_parameter.RBF:
                return Math.exp(-gamma * (x_square[i] + x_square[j] - 2 * dot(x[i], x[j])));
            case svm_parameter.SIGMOID:
                return Math.tanh(gamma * dot(x[i], x[j]) + coef0);
            default:
                return 0;	// java
        }
    }

    Kernel(int l, svm_node[][] x_, svm_parameter param) {
        this.kernel_type = param.kernel_type;
        this.degree = param.degree;
        this.gamma = param.gamma;
        this.coef0 = param.coef0;

        x = (svm_node[][]) x_.clone();

        if (kernel_type == svm_parameter.RBF) {
            x_square = new double[l];
            for (int i = 0; i < l; i++) {
                x_square[i] = dot(x[i], x[i]);
            }
        } else {
            x_square = null;
        }
    }

    //OK
    static double dot(svm_node[] x, svm_node[] y) {
        double sum = 0;
        int xlen = x.length;
        int ylen = y.length;
        int i = 0;
        int j = 0;
        while (i < xlen && j < ylen) {
            if (x[i].index == y[j].index) {
                sum += x[i++].value * y[j++].value;
            } else {
                if (x[i].index > y[j].index) {
                    ++j;
                } else {
                    ++i;
                }
            }
        }
        return sum;
    }

    //OK
    static double k_function(svm_node[] x, svm_node[] y,
            svm_parameter param) {
        switch (param.kernel_type) {
            case svm_parameter.LINEAR:
                return dot(x, y);
            case svm_parameter.POLY:
                return powi(param.gamma * dot(x, y) + param.coef0, param.degree);
            case svm_parameter.RBF: {
                double sum = 0;
                int xlen = x.length;
                int ylen = y.length;
                int i = 0;
                int j = 0;
                while (i < xlen && j < ylen) {
                    if (x[i].index == y[j].index) {
                        double d = x[i++].value - y[j++].value;
                        sum += d * d;
                    } else if (x[i].index > y[j].index) {
                        sum += y[j].value * y[j].value;
                        ++j;
                    } else {
                        sum += x[i].value * x[i].value;
                        ++i;
                    }
                }

                while (i < xlen) {
                    sum += x[i].value * x[i].value;
                    ++i;
                }

                while (j < ylen) {
                    sum += y[j].value * y[j].value;
                    ++j;
                }

                return Math.exp(-param.gamma * sum);
            }
            case svm_parameter.SIGMOID:
                return Math.tanh(param.gamma * dot(x, y) + param.coef0);
            default:
                return 0;	// java
        }
    }
}

class Solver {

    int active_size;
    byte[] y;
    double[] G;		// gradient of objective function
    static final byte LOWER_BOUND = 0;
    static final byte UPPER_BOUND = 1;
    static final byte FREE = 2;
    byte[] alpha_status;	// LOWER_BOUND, UPPER_BOUND, FREE
    double[] alpha;
    QMatrix Q;
    double[] QD;
    double eps;
    double Cp, Cn;
    double[] p;
    int[] active_set;
    double[] G_bar;		// gradient, if we treat free variables as 0
    int l;
    boolean unshrink;	// XXX

    static final double INF = java.lang.Double.POSITIVE_INFINITY;

    double get_C(int i) {
        return (y[i] > 0) ? Cp : Cn;
    }

    void update_alpha_status(int i) {
        if (alpha[i] >= get_C(i)) {
            alpha_status[i] = UPPER_BOUND;
        } else if (alpha[i] <= 0) {
            alpha_status[i] = LOWER_BOUND;
        } else {
            alpha_status[i] = FREE;
        }
    }

    boolean is_upper_bound(int i) {
        return alpha_status[i] == UPPER_BOUND;
    }

    boolean is_lower_bound(int i) {
        return alpha_status[i] == LOWER_BOUND;
    }

    boolean is_free(int i) {
        return alpha_status[i] == FREE;
    }

    // java: information about solution except alpha,
    // because we cannot return multiple values otherwise...
    static class SolutionInfo {

        double obj;
        double rho;
        double upper_bound_p;
        double upper_bound_n;
        double r;	// for Solver_NU
    }

    void swap_index(int i, int j) {
        Q.swap_index(i, j);
        do {
            byte aux = y[i];
            y[i] = y[j];
            y[j] = aux;
        } while (false);
        do {
            double aux = G[i];
            G[i] = G[j];
            G[j] = aux;
        } while (false);
        do {
            byte aux = alpha_status[i];
            alpha_status[i] = alpha_status[j];
            alpha_status[j] = aux;
        } while (false);
        do {
            double aux = alpha[i];
            alpha[i] = alpha[j];
            alpha[j] = aux;
        } while (false);
        do {
            double aux = p[i];
            p[i] = p[j];
            p[j] = aux;
        } while (false);
        do {
            int aux = active_set[i];
            active_set[i] = active_set[j];
            active_set[j] = aux;
        } while (false);
        do {
            double aux = G_bar[i];
            G_bar[i] = G_bar[j];
            G_bar[j] = aux;
        } while (false);
    }

    void reconstruct_gradient() {
        // reconstruct inactive elements of G from G_bar and free variables

        if (active_size == l) {
            return;
        }

        int i, j;
        int nr_free = 0;

        for (j = active_size; j < l; j++) {
            G[j] = G_bar[j] + p[j];
        }

        for (j = 0; j < active_size; j++) {
            if (is_free(j)) {
                nr_free++;
            }
        }

        if (nr_free * l > 2 * active_size * (l - active_size)) {
            for (i = active_size; i < l; i++) {
                float[] Q_i = Q.get_Q(i, active_size);
                for (j = 0; j < active_size; j++) {
                    if (is_free(j)) {
                        G[i] += alpha[j] * Q_i[j];
                    }
                }
            }
        } else {
            for (i = 0; i < active_size; i++) {
                if (is_free(i)) {
                    float[] Q_i = Q.get_Q(i, l);
                    double alpha_i = alpha[i];
                    for (j = active_size; j < l; j++) {
                        G[j] += alpha_i * Q_i[j];
                    }
                }
            }
        }
    }

    void Solve(int l, QMatrix Q, double[] p_, byte[] y_,
            double[] alpha_, double Cp, double Cn, double eps, SolutionInfo si, int shrinking) {
        this.l = l;
        this.Q = Q;
        QD = Q.get_QD();
        p = (double[]) p_.clone();
        y = (byte[]) y_.clone();
        alpha = (double[]) alpha_.clone();
        this.Cp = Cp;
        this.Cn = Cn;
        this.eps = eps;
        this.unshrink = false;

        // initialize alpha_status
        {
            alpha_status = new byte[l];
            for (int i = 0; i < l; i++) {
                update_alpha_status(i);
            }
        }

        // initialize active set (for shrinking)
        {
            active_set = new int[l];
            for (int i = 0; i < l; i++) {
                active_set[i] = i;
            }
            active_size = l;
        }

        // initialize gradient
        {
            G = new double[l];
            G_bar = new double[l];
            int i;
            for (i = 0; i < l; i++) {
                G[i] = p[i];
                G_bar[i] = 0;
            }
            for (i = 0; i < l; i++) {
                if (!is_lower_bound(i)) {
                    float[] Q_i = Q.get_Q(i, l);
                    double alpha_i = alpha[i];
                    int j;
                    for (j = 0; j < l; j++) {
                        G[j] += alpha_i * Q_i[j];
                    }
                    if (is_upper_bound(i)) {
                        for (j = 0; j < l; j++) {
                            G_bar[j] += get_C(i) * Q_i[j];
                        }
                    }
                }
            }
        }

        // optimization step
        int iter = 0;
        int max_iter = Math.max(10000000, l > Integer.MAX_VALUE / 100 ? Integer.MAX_VALUE : 100 * l);
        int counter = Math.min(l, 1000) + 1;
        int[] working_set = new int[2];

        while (iter < max_iter) {
            // show progress and do shrinking

            if (--counter == 0) {
                counter = Math.min(l, 1000);
                if (shrinking != 0) {
                    do_shrinking();
                }
            }

            if (select_working_set(working_set) != 0) {
                // reconstruct the whole gradient
                reconstruct_gradient();
                // reset active set size and check
                active_size = l;
                if (select_working_set(working_set) != 0) {
                    break;
                } else {
                    counter = 1;	// do shrinking next iteration
                }
            }

            int i = working_set[0];
            int j = working_set[1];

            ++iter;

            // update alpha[i] and alpha[j], handle bounds carefully
            float[] Q_i = Q.get_Q(i, active_size);
            float[] Q_j = Q.get_Q(j, active_size);

            double C_i = get_C(i);
            double C_j = get_C(j);

            double old_alpha_i = alpha[i];
            double old_alpha_j = alpha[j];

            if (y[i] != y[j]) {
                double quad_coef = QD[i] + QD[j] + 2 * Q_i[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12;
                }
                double delta = (-G[i] - G[j]) / quad_coef;
                double diff = alpha[i] - alpha[j];
                alpha[i] += delta;
                alpha[j] += delta;

                if (diff > 0) {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = diff;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = -diff;
                    }
                }
                if (diff > C_i - C_j) {
                    if (alpha[i] > C_i) {
                        alpha[i] = C_i;
                        alpha[j] = C_i - diff;
                    }
                } else {
                    if (alpha[j] > C_j) {
                        alpha[j] = C_j;
                        alpha[i] = C_j + diff;
                    }
                }
            } else {
                double quad_coef = QD[i] + QD[j] - 2 * Q_i[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12;
                }
                double delta = (G[i] - G[j]) / quad_coef;
                double sum = alpha[i] + alpha[j];
                alpha[i] -= delta;
                alpha[j] += delta;

                if (sum > C_i) {
                    if (alpha[i] > C_i) {
                        alpha[i] = C_i;
                        alpha[j] = sum - C_i;
                    }
                } else {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = sum;
                    }
                }
                if (sum > C_j) {
                    if (alpha[j] > C_j) {
                        alpha[j] = C_j;
                        alpha[i] = sum - C_j;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = sum;
                    }
                }
            }

            // update G
            double delta_alpha_i = alpha[i] - old_alpha_i;
            double delta_alpha_j = alpha[j] - old_alpha_j;

            for (int k = 0; k < active_size; k++) {
                G[k] += Q_i[k] * delta_alpha_i + Q_j[k] * delta_alpha_j;
            }

            // update alpha_status and G_bar
            {
                boolean ui = is_upper_bound(i);
                boolean uj = is_upper_bound(j);
                update_alpha_status(i);
                update_alpha_status(j);
                int k;
                if (ui != is_upper_bound(i)) {
                    Q_i = Q.get_Q(i, l);
                    if (ui) {
                        for (k = 0; k < l; k++) {
                            G_bar[k] -= C_i * Q_i[k];
                        }
                    } else {
                        for (k = 0; k < l; k++) {
                            G_bar[k] += C_i * Q_i[k];
                        }
                    }
                }

                if (uj != is_upper_bound(j)) {
                    Q_j = Q.get_Q(j, l);
                    if (uj) {
                        for (k = 0; k < l; k++) {
                            G_bar[k] -= C_j * Q_j[k];
                        }
                    } else {
                        for (k = 0; k < l; k++) {
                            G_bar[k] += C_j * Q_j[k];
                        }
                    }
                }
            }

        }

        if (iter >= max_iter) {
            if (active_size < l) {
                // reconstruct the whole gradient to calculate objective value
                reconstruct_gradient();
                active_size = l;
            }
        }

        // calculate rho
        si.rho = calculate_rho();

        // calculate objective value
        {
            double v = 0;
            int i;
            for (i = 0; i < l; i++) {
                v += alpha[i] * (G[i] + p[i]);
            }

            si.obj = v / 2;
        }

        // put back the solution
        {
            for (int i = 0; i < l; i++) {
                alpha_[active_set[i]] = alpha[i];
            }
        }

        si.upper_bound_p = Cp;
        si.upper_bound_n = Cn;

    }

    // return 1 if already optimal, return 0 otherwise
    int select_working_set(int[] working_set) {
        // return i,j such that
        // i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
        // j: mimimizes the decrease of obj value
        //    (if quadratic coefficeint <= 0, replace it with tau)
        //    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)

        double Gmax = -INF;
        double Gmax2 = -INF;
        int Gmax_idx = -1;
        int Gmin_idx = -1;
        double obj_diff_min = INF;

        for (int t = 0; t < active_size; t++) {
            if (y[t] == +1) {
                if (!is_upper_bound(t)) {
                    if (-G[t] >= Gmax) {
                        Gmax = -G[t];
                        Gmax_idx = t;
                    }
                }
            } else {
                if (!is_lower_bound(t)) {
                    if (G[t] >= Gmax) {
                        Gmax = G[t];
                        Gmax_idx = t;
                    }
                }
            }
        }

        int i = Gmax_idx;
        float[] Q_i = null;
        if (i != -1) // null Q_i not accessed: Gmax=-INF if i=-1
        {
            Q_i = Q.get_Q(i, active_size);
        }

        for (int j = 0; j < active_size; j++) {
            if (y[j] == +1) {
                if (!is_lower_bound(j)) {
                    double grad_diff = Gmax + G[j];
                    if (G[j] >= Gmax2) {
                        Gmax2 = G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = QD[i] + QD[j] - 2.0 * y[i] * Q_i[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            } else {
                if (!is_upper_bound(j)) {
                    double grad_diff = Gmax - G[j];
                    if (-G[j] >= Gmax2) {
                        Gmax2 = -G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = QD[i] + QD[j] + 2.0 * y[i] * Q_i[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            }
        }

        if (Gmax + Gmax2 < eps || Gmin_idx == -1) {
            return 1;
        }

        working_set[0] = Gmax_idx;
        working_set[1] = Gmin_idx;
        return 0;
    }

    private boolean be_shrunk(int i, double Gmax1, double Gmax2) {
        if (is_upper_bound(i)) {
            if (y[i] == +1) {
                return (-G[i] > Gmax1);
            } else {
                return (-G[i] > Gmax2);
            }
        } else if (is_lower_bound(i)) {
            if (y[i] == +1) {
                return (G[i] > Gmax2);
            } else {
                return (G[i] > Gmax1);
            }
        } else {
            return (false);
        }
    }

    void do_shrinking() {
        int i;
        double Gmax1 = -INF;		// max { -y_i * grad(f)_i | i in I_up(\alpha) }
        double Gmax2 = -INF;		// max { y_i * grad(f)_i | i in I_low(\alpha) }

        // find maximal violating pair first
        for (i = 0; i < active_size; i++) {
            if (y[i] == +1) {
                if (!is_upper_bound(i)) {
                    if (-G[i] >= Gmax1) {
                        Gmax1 = -G[i];
                    }
                }
                if (!is_lower_bound(i)) {
                    if (G[i] >= Gmax2) {
                        Gmax2 = G[i];
                    }
                }
            } else {
                if (!is_upper_bound(i)) {
                    if (-G[i] >= Gmax2) {
                        Gmax2 = -G[i];
                    }
                }
                if (!is_lower_bound(i)) {
                    if (G[i] >= Gmax1) {
                        Gmax1 = G[i];
                    }
                }
            }
        }

        if (unshrink == false && Gmax1 + Gmax2 <= eps * 10) {
            unshrink = true;
            reconstruct_gradient();
            active_size = l;
        }

        for (i = 0; i < active_size; i++) {
            if (be_shrunk(i, Gmax1, Gmax2)) {
                active_size--;
                while (active_size > i) {
                    if (!be_shrunk(active_size, Gmax1, Gmax2)) {
                        swap_index(i, active_size);
                        break;
                    }
                    active_size--;
                }
            }
        }
    }

    double calculate_rho() {
        double r;
        int nr_free = 0;
        double ub = INF, lb = -INF, sum_free = 0;
        for (int i = 0; i < active_size; i++) {
            double yG = y[i] * G[i];

            if (is_lower_bound(i)) {
                if (y[i] > 0) {
                    ub = Math.min(ub, yG);
                } else {
                    lb = Math.max(lb, yG);
                }
            } else if (is_upper_bound(i)) {
                if (y[i] < 0) {
                    ub = Math.min(ub, yG);
                } else {
                    lb = Math.max(lb, yG);
                }
            } else {
                ++nr_free;
                sum_free += yG;
            }
        }

        if (nr_free > 0) {
            r = sum_free / nr_free;
        } else {
            r = (ub + lb) / 2;
        }

        return r;
    }

}

//
// Solver for nu-svm classification and regression
//
// additional constraint: e^T \alpha = constant
//
final class Solver_NU extends Solver {

    private SolutionInfo si;

    @Override
    void Solve(int l, QMatrix Q, double[] p, byte[] y,
            double[] alpha, double Cp, double Cn, double eps,
            SolutionInfo si, int shrinking) {
        this.si = si;
        super.Solve(l, Q, p, y, alpha, Cp, Cn, eps, si, shrinking);
    }

    // return 1 if already optimal, return 0 otherwise
    int select_working_set(int[] working_set) {
        // return i,j such that y_i = y_j and
        // i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
        // j: minimizes the decrease of obj value
        //    (if quadratic coefficeint <= 0, replace it with tau)
        //    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)

        double Gmaxp = -INF;
        double Gmaxp2 = -INF;
        int Gmaxp_idx = -1;

        double Gmaxn = -INF;
        double Gmaxn2 = -INF;
        int Gmaxn_idx = -1;

        int Gmin_idx = -1;
        double obj_diff_min = INF;

        for (int t = 0; t < active_size; t++) {
            if (y[t] == +1) {
                if (!is_upper_bound(t)) {
                    if (-G[t] >= Gmaxp) {
                        Gmaxp = -G[t];
                        Gmaxp_idx = t;
                    }
                }
            } else {
                if (!is_lower_bound(t)) {
                    if (G[t] >= Gmaxn) {
                        Gmaxn = G[t];
                        Gmaxn_idx = t;
                    }
                }
            }
        }

        int ip = Gmaxp_idx;
        int in = Gmaxn_idx;
        float[] Q_ip = null;
        float[] Q_in = null;
        if (ip != -1) // null Q_ip not accessed: Gmaxp=-INF if ip=-1
        {
            Q_ip = Q.get_Q(ip, active_size);
        }
        if (in != -1) {
            Q_in = Q.get_Q(in, active_size);
        }

        for (int j = 0; j < active_size; j++) {
            if (y[j] == +1) {
                if (!is_lower_bound(j)) {
                    double grad_diff = Gmaxp + G[j];
                    if (G[j] >= Gmaxp2) {
                        Gmaxp2 = G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = QD[ip] + QD[j] - 2 * Q_ip[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            } else {
                if (!is_upper_bound(j)) {
                    double grad_diff = Gmaxn - G[j];
                    if (-G[j] >= Gmaxn2) {
                        Gmaxn2 = -G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = QD[in] + QD[j] - 2 * Q_in[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            }
        }

        if (Math.max(Gmaxp + Gmaxp2, Gmaxn + Gmaxn2) < eps || Gmin_idx == -1) {
            return 1;
        }

        if (y[Gmin_idx] == +1) {
            working_set[0] = Gmaxp_idx;
        } else {
            working_set[0] = Gmaxn_idx;
        }
        working_set[1] = Gmin_idx;

        return 0;
    }

    private boolean be_shrunk(int i, double Gmax1, double Gmax2, double Gmax3, double Gmax4) {
        if (is_upper_bound(i)) {
            if (y[i] == +1) {
                return (-G[i] > Gmax1);
            } else {
                return (-G[i] > Gmax4);
            }
        } else if (is_lower_bound(i)) {
            if (y[i] == +1) {
                return (G[i] > Gmax2);
            } else {
                return (G[i] > Gmax3);
            }
        } else {
            return (false);
        }
    }

    void do_shrinking() {
        double Gmax1 = -INF;	// max { -y_i * grad(f)_i | y_i = +1, i in I_up(\alpha) }
        double Gmax2 = -INF;	// max { y_i * grad(f)_i | y_i = +1, i in I_low(\alpha) }
        double Gmax3 = -INF;	// max { -y_i * grad(f)_i | y_i = -1, i in I_up(\alpha) }
        double Gmax4 = -INF;	// max { y_i * grad(f)_i | y_i = -1, i in I_low(\alpha) }

        // find maximal violating pair first
        int i;
        for (i = 0; i < active_size; i++) {
            if (!is_upper_bound(i)) {
                if (y[i] == +1) {
                    if (-G[i] > Gmax1) {
                        Gmax1 = -G[i];
                    }
                } else if (-G[i] > Gmax4) {
                    Gmax4 = -G[i];
                }
            }
            if (!is_lower_bound(i)) {
                if (y[i] == +1) {
                    if (G[i] > Gmax2) {
                        Gmax2 = G[i];
                    }
                } else if (G[i] > Gmax3) {
                    Gmax3 = G[i];
                }
            }
        }

        if (unshrink == false && Math.max(Gmax1 + Gmax2, Gmax3 + Gmax4) <= eps * 10) {
            unshrink = true;
            reconstruct_gradient();
            active_size = l;
        }

        for (i = 0; i < active_size; i++) {
            if (be_shrunk(i, Gmax1, Gmax2, Gmax3, Gmax4)) {
                active_size--;
                while (active_size > i) {
                    if (!be_shrunk(active_size, Gmax1, Gmax2, Gmax3, Gmax4)) {
                        swap_index(i, active_size);
                        break;
                    }
                    active_size--;
                }
            }
        }
    }

    double calculate_rho() {
        int nr_free1 = 0, nr_free2 = 0;
        double ub1 = INF, ub2 = INF;
        double lb1 = -INF, lb2 = -INF;
        double sum_free1 = 0, sum_free2 = 0;

        for (int i = 0; i < active_size; i++) {
            if (y[i] == +1) {
                if (is_lower_bound(i)) {
                    ub1 = Math.min(ub1, G[i]);
                } else if (is_upper_bound(i)) {
                    lb1 = Math.max(lb1, G[i]);
                } else {
                    ++nr_free1;
                    sum_free1 += G[i];
                }
            } else {
                if (is_lower_bound(i)) {
                    ub2 = Math.min(ub2, G[i]);
                } else if (is_upper_bound(i)) {
                    lb2 = Math.max(lb2, G[i]);
                } else {
                    ++nr_free2;
                    sum_free2 += G[i];
                }
            }
        }

        double r1, r2;
        if (nr_free1 > 0) {
            r1 = sum_free1 / nr_free1;
        } else {
            r1 = (ub1 + lb1) / 2;
        }

        if (nr_free2 > 0) {
            r2 = sum_free2 / nr_free2;
        } else {
            r2 = (ub2 + lb2) / 2;
        }

        si.r = (r1 + r2) / 2;
        return (r1 - r2) / 2;
    }
}

//OK
class SVR_Q extends Kernel {

    private final int l;
    private final Cache cache;
    private final byte[] sign;
    private final int[] index;
    private int next_buffer;
    private float[][] buffer;
    private final double[] QD;

    SVR_Q(svm_problem prob, svm_parameter param) {
        super(prob.l, prob.x, param);
        l = prob.l;
        cache = new Cache(l, (long) (param.cache_size * (1 << 20)));
        QD = new double[2 * l];
        sign = new byte[2 * l];
        index = new int[2 * l];
        for (int k = 0; k < l; k++) {
            sign[k] = 1;
            sign[k + l] = -1;
            index[k] = k;
            index[k + l] = k;
            QD[k] = kernel_function(k, k);
            QD[k + l] = QD[k];
        }
        buffer = new float[2][2 * l];
        next_buffer = 0;
    }

    void swap_index(int i, int j) {
        do {
            byte aux = sign[i];
            sign[i] = sign[j];
            sign[j] = aux;
        } while (false);
        do {
            int aux = index[i];
            index[i] = index[j];
            index[j] = aux;
        } while (false);
        do {
            double aux = QD[i];
            QD[i] = QD[j];
            QD[j] = aux;
        } while (false);
    }

    float[] get_Q(int i, int len) {
        float[][] data = new float[1][];
        int j, real_i = index[i];
        if (cache.get_data(real_i, data, l) < l) {
            for (j = 0; j < l; j++) {
                data[0][j] = (float) kernel_function(real_i, j);
            }
        }

        // reorder and copy
        float buf[] = buffer[next_buffer];
        next_buffer = 1 - next_buffer;
        byte si = sign[i];
        for (j = 0; j < len; j++) {
            buf[j] = (float) si * sign[j] * data[0][index[j]];
        }
        return buf;
    }

    double[] get_QD() {
        return QD;
    }
}

//OK
public class svm {
    //
    // construct and solve various formulations
    //

    public static final int LIBSVM_VERSION = 321;
    public static final Random rand = new Random();

    //OK
    private static void solve_epsilon_svr(svm_problem prob, svm_parameter param,
            double[] alpha, Solver.SolutionInfo si) {
        int l = prob.l;
        double[] alpha2 = new double[2 * l];
        double[] linear_term = new double[2 * l];
        byte[] y = new byte[2 * l];
        int i;

        for (i = 0; i < l; i++) {
            alpha2[i] = 0;
            linear_term[i] = param.p - prob.y[i];
            y[i] = 1;

            alpha2[i + l] = 0;
            linear_term[i + l] = param.p + prob.y[i];
            y[i + l] = -1;
        }

        Solver s = new Solver();
        s.Solve(2 * l, new SVR_Q(prob, param), linear_term, y,
                alpha2, param.C, param.C, param.eps, si, param.shrinking);

        double sum_alpha = 0;
        for (i = 0; i < l; i++) {
            alpha[i] = alpha2[i] - alpha2[i + l];
            sum_alpha += Math.abs(alpha[i]);
        }

    }

    //OK
    private static void solve_nu_svr(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
        int l = prob.l;
        double C = param.C;
        double[] alpha2 = new double[2 * l];
        double[] linear_term = new double[2 * l];
        byte[] y = new byte[2 * l];
        int i;

        double sum = C * param.nu * l / 2;
        for (i = 0; i < l; i++) {
            alpha2[i] = alpha2[i + l] = Math.min(sum, C);
            sum -= alpha2[i];

            linear_term[i] = -prob.y[i];
            y[i] = 1;

            linear_term[i + l] = prob.y[i];
            y[i + l] = -1;
        }

        Solver_NU s = new Solver_NU();
        s.Solve(2 * l, new SVR_Q(prob, param), linear_term, y, alpha2, C, C, param.eps, si, param.shrinking);

        for (i = 0; i < l; i++) {
            alpha[i] = alpha2[i] - alpha2[i + l];
        }
    }

    //
    // decision_function
    //
    //OK
    static class decision_function {

        double[] alpha;
        double rho;
    };

    //OK
    static decision_function svm_train_one(svm_problem prob, svm_parameter param) {
        double[] alpha = new double[prob.l];
        Solver.SolutionInfo si = new Solver.SolutionInfo();
        switch (param.svm_type) {
            case svm_parameter.EPSILON_SVR:
                solve_epsilon_svr(prob, param, alpha, si);
                break;
            case svm_parameter.NU_SVR:
                solve_nu_svr(prob, param, alpha, si);
                break;
        }

        decision_function f = new decision_function();
        f.alpha = alpha;
        f.rho = si.rho;
        return f;
    }

    // Return parameter of a Laplace distribution
    //OK
    private static double svm_svr_probability(svm_problem prob, svm_parameter param) {
        int i;
        int nr_fold = 5;
        double[] ymv = new double[prob.l];
        double mae = 0;

        svm_parameter newparam = (svm_parameter) param.clone();
        newparam.probability = 0;
        svm_cross_validation(prob, newparam, nr_fold, ymv);
        for (i = 0; i < prob.l; i++) {
            ymv[i] = prob.y[i] - ymv[i];
            mae += Math.abs(ymv[i]);
        }
        mae /= prob.l;
        double std = Math.sqrt(2 * mae * mae);
        int count = 0;
        mae = 0;
        for (i = 0; i < prob.l; i++) {
            if (Math.abs(ymv[i]) > 5 * std) {
                count = count + 1;
            } else {
                mae += Math.abs(ymv[i]);
            }
        }
        mae /= (prob.l - count);
        return mae;
    }

    //
    // Interface functions
    //
    //OK
    public static svm_model svm_train(svm_problem prob, svm_parameter param) {
        svm_model model = new svm_model();
        model.param = param;

        // regression
        model.nr_class = 2;
        model.label = null;
        model.nSV = null;
        model.probA = null;
        model.probB = null;
        model.sv_coef = new double[1][];

        if (param.probability == 1) {
            model.probA = new double[1];
            model.probA[0] = svm_svr_probability(prob, param);
        }

        decision_function f = svm_train_one(prob, param);
        model.rho = new double[1];
        model.rho[0] = f.rho;

        int nSV = 0;
        int i;
        for (i = 0; i < prob.l; i++) {
            if (Math.abs(f.alpha[i]) > 0) {
                ++nSV;
            }
        }
        model.l = nSV;
        model.SV = new svm_node[nSV][];
        model.sv_coef[0] = new double[nSV];
        model.sv_indices = new int[nSV];
        int j = 0;
        for (i = 0; i < prob.l; i++) {
            if (Math.abs(f.alpha[i]) > 0) {
                model.SV[j] = prob.x[i];
                model.sv_coef[0][j] = f.alpha[i];
                model.sv_indices[j] = i + 1;
                ++j;
            }
        }
        return model;
    }

    // Stratified cross validation
    //OK
    public static void svm_cross_validation(svm_problem prob, svm_parameter param, int nr_fold, double[] target) {
        int i;
        int[] fold_start = new int[nr_fold + 1];
        int l = prob.l;
        int[] perm = new int[l];

        for (i = 0; i < l; i++) {
            perm[i] = i;
        }
        for (i = 0; i < l; i++) {
            int j = i + rand.nextInt(l - i);
            do {
                int aux = perm[i];
                perm[i] = perm[j];
                perm[j] = aux;
            } while (false);
        }
        for (i = 0; i <= nr_fold; i++) {
            fold_start[i] = i * l / nr_fold;
        }

        for (i = 0; i < nr_fold; i++) {
            int begin = fold_start[i];
            int end = fold_start[i + 1];
            int j, k;
            svm_problem subprob = new svm_problem();

            subprob.l = l - (end - begin);
            subprob.x = new svm_node[subprob.l][];
            subprob.y = new double[subprob.l];

            k = 0;
            for (j = 0; j < begin; j++) {
                subprob.x[k] = prob.x[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            for (j = end; j < l; j++) {
                subprob.x[k] = prob.x[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            svm_model submodel = svm_train(subprob, param);

            for (j = begin; j < end; j++) {
                target[perm[j]] = svm_predict(submodel, prob.x[perm[j]]);
            }
        }
    }

    //OK
    public static int svm_get_svm_type(svm_model model) {
        return model.param.svm_type;
    }

    //OK
    public static int svm_get_nr_class(svm_model model) {
        return model.nr_class;
    }

    //OK
    public static void svm_get_labels(svm_model model, int[] label) {
        if (model.label != null) {
            for (int i = 0; i < model.nr_class; i++) {
                label[i] = model.label[i];
            }
        }
    }

    //OK
    public static void svm_get_sv_indices(svm_model model, int[] indices) {
        if (model.sv_indices != null) {
            for (int i = 0; i < model.l; i++) {
                indices[i] = model.sv_indices[i];
            }
        }
    }

    //OK
    public static int svm_get_nr_sv(svm_model model) {
        return model.l;
    }

    //OK
    public static double svm_get_svr_probability(svm_model model) {
        if (model.probA != null) {
            return model.probA[0];
        } else {
            return 0;
        }
    }

    //OK
    public static double svm_predict_values(svm_model model, svm_node[] x, double[] dec_values) {
        int i;
        double[] sv_coef = model.sv_coef[0];
        double sum = 0;
        for (i = 0; i < model.l; i++) {
            sum += sv_coef[i] * Kernel.k_function(x, model.SV[i], model.param);
        }
        sum -= model.rho[0];
        dec_values[0] = sum;

        return sum;
    }

    //OK
    public static double svm_predict(svm_model model, svm_node[] x) {
        double[] dec_values;
        dec_values = new double[1];
        double pred_result = svm_predict_values(model, x, dec_values);
        return pred_result;
    }

    //OK
    public static double svm_predict_probability(svm_model model, svm_node[] x, double[] prob_estimates) {
        return svm_predict(model, x);
    }

    //OK
    static final String svm_type_table[] = {
        "c_svc", "nu_svc", "one_class", "epsilon_svr", "nu_svr", };

    //OK
    static final String kernel_type_table[] = {
        "linear", "polynomial", "rbf", "sigmoid", "precomputed"
    };

    //OK
    public static void svm_save_model(String model_file_name, svm_model model) throws IOException {
        DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(model_file_name)));

        svm_parameter param = model.param;

        fp.writeBytes("svm_type " + svm_type_table[param.svm_type] + "\n");
        fp.writeBytes("kernel_type " + kernel_type_table[param.kernel_type] + "\n");

        if (param.kernel_type == svm_parameter.POLY) {
            fp.writeBytes("degree " + param.degree + "\n");
        }

        if (param.kernel_type == svm_parameter.POLY ||
                param.kernel_type == svm_parameter.RBF ||
                param.kernel_type == svm_parameter.SIGMOID) {
            fp.writeBytes("gamma " + param.gamma + "\n");
        }

        if (param.kernel_type == svm_parameter.POLY ||
                param.kernel_type == svm_parameter.SIGMOID) {
            fp.writeBytes("coef0 " + param.coef0 + "\n");
        }

        int nr_class = model.nr_class;
        int l = model.l;
        fp.writeBytes("nr_class " + nr_class + "\n");
        fp.writeBytes("total_sv " + l + "\n");

        {
            fp.writeBytes("rho");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                fp.writeBytes(" " + model.rho[i]);
            }
            fp.writeBytes("\n");
        }

        if (model.label != null) {
            fp.writeBytes("label");
            for (int i = 0; i < nr_class; i++) {
                fp.writeBytes(" " + model.label[i]);
            }
            fp.writeBytes("\n");
        }

        if (model.probA != null) // regression has probA only
        {
            fp.writeBytes("probA");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                fp.writeBytes(" " + model.probA[i]);
            }
            fp.writeBytes("\n");
        }
        if (model.probB != null) {
            fp.writeBytes("probB");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                fp.writeBytes(" " + model.probB[i]);
            }
            fp.writeBytes("\n");
        }

        if (model.nSV != null) {
            fp.writeBytes("nr_sv");
            for (int i = 0; i < nr_class; i++) {
                fp.writeBytes(" " + model.nSV[i]);
            }
            fp.writeBytes("\n");
        }

        fp.writeBytes("SV\n");
        double[][] sv_coef = model.sv_coef;
        svm_node[][] SV = model.SV;

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < nr_class - 1; j++) {
                fp.writeBytes(sv_coef[j][i] + " ");
            }

            svm_node[] p = SV[i];
            for (int j = 0; j < p.length; j++) {
                fp.writeBytes(p[j].index + ":" + p[j].value + " ");
            }
            fp.writeBytes("\n");
        }

        fp.close();
    }

    //OK
    private static double atof(String s) {
        return Double.valueOf(s);
    }

    //OK
    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    //OK
    private static boolean read_model_header(BufferedReader fp, svm_model model) {
        svm_parameter param = new svm_parameter();
        model.param = param;
        try {
            while (true) {
                String cmd = fp.readLine();
                String arg = cmd.substring(cmd.indexOf(' ') + 1);

                if (cmd.startsWith("svm_type")) {
                    int i;
                    for (i = 0; i < svm_type_table.length; i++) {
                        if (arg.contains(svm_type_table[i])) {
                            param.svm_type = i;
                            break;
                        }
                    }
                    if (i == svm_type_table.length) {
                        return false;
                    }
                } else if (cmd.startsWith("kernel_type")) {
                    int i;
                    for (i = 0; i < kernel_type_table.length; i++) {
                        if (arg.contains(kernel_type_table[i])) {
                            param.kernel_type = i;
                            break;
                        }
                    }
                    if (i == kernel_type_table.length) {
                        return false;
                    }
                } else if (cmd.startsWith("degree")) {
                    param.degree = atoi(arg);
                } else if (cmd.startsWith("gamma")) {
                    param.gamma = atof(arg);
                } else if (cmd.startsWith("coef0")) {
                    param.coef0 = atof(arg);
                } else if (cmd.startsWith("nr_class")) {
                    model.nr_class = atoi(arg);
                } else if (cmd.startsWith("total_sv")) {
                    model.l = atoi(arg);
                } else if (cmd.startsWith("rho")) {
                    int n = model.nr_class * (model.nr_class - 1) / 2;
                    model.rho = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++) {
                        model.rho[i] = atof(st.nextToken());
                    }
                } else if (cmd.startsWith("label")) {
                    int n = model.nr_class;
                    model.label = new int[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++) {
                        model.label[i] = atoi(st.nextToken());
                    }
                } else if (cmd.startsWith("probA")) {
                    int n = model.nr_class * (model.nr_class - 1) / 2;
                    model.probA = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++) {
                        model.probA[i] = atof(st.nextToken());
                    }
                } else if (cmd.startsWith("probB")) {
                    int n = model.nr_class * (model.nr_class - 1) / 2;
                    model.probB = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++) {
                        model.probB[i] = atof(st.nextToken());
                    }
                } else if (cmd.startsWith("nr_sv")) {
                    int n = model.nr_class;
                    model.nSV = new int[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++) {
                        model.nSV[i] = atoi(st.nextToken());
                    }
                } else if (cmd.startsWith("SV")) {
                    break;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //OK
    public static svm_model svm_load_model(String model_file_name) throws IOException {
        return svm_load_model(new BufferedReader(new FileReader(model_file_name)));
    }

    //OK
    public static svm_model svm_load_model(BufferedReader fp) throws IOException {
        // read parameters

        svm_model model = new svm_model();
        model.rho = null;
        model.probA = null;
        model.probB = null;
        model.label = null;
        model.nSV = null;

        if (read_model_header(fp, model) == false) {

            return null;
        }

        // read sv_coef and SV
        int m = model.nr_class - 1;
        int l = model.l;
        model.sv_coef = new double[m][l];
        model.SV = new svm_node[l][];

        for (int i = 0; i < l; i++) {
            String line = fp.readLine();
            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            for (int k = 0; k < m; k++) {
                model.sv_coef[k][i] = atof(st.nextToken());
            }
            int n = st.countTokens() / 2;
            model.SV[i] = new svm_node[n];
            for (int j = 0; j < n; j++) {
                model.SV[i][j] = new svm_node();
                model.SV[i][j].index = atoi(st.nextToken());
                model.SV[i][j].value = atof(st.nextToken());
            }
        }

        fp.close();
        return model;
    }

    //OK
    public static String svm_check_parameter(svm_problem prob, svm_parameter param) {
        // svm_type

        int svm_type = param.svm_type;
        if (svm_type != svm_parameter.EPSILON_SVR &&
                svm_type != svm_parameter.NU_SVR) {
            return "unknown svm type";
        }

        // kernel_type, degree
        int kernel_type = param.kernel_type;
        if (kernel_type != svm_parameter.LINEAR &&
                kernel_type != svm_parameter.POLY &&
                kernel_type != svm_parameter.RBF &&
                kernel_type != svm_parameter.SIGMOID) {
            return "unknown kernel type";
        }

        if (param.gamma < 0) {
            return "gamma < 0";
        }

        if (param.degree < 0) {
            return "degree of polynomial kernel < 0";
        }

        // cache_size,eps,C,nu,p,shrinking
        if (param.cache_size <= 0) {
            return "cache_size <= 0";
        }

        if (param.eps <= 0) {
            return "eps <= 0";
        }

        if (svm_type == svm_parameter.EPSILON_SVR ||
                svm_type == svm_parameter.NU_SVR) {
            if (param.C <= 0) {
                return "C <= 0";
            }
        }

        if (svm_type == svm_parameter.NU_SVR) {
            if (param.nu <= 0 || param.nu > 1) {
                return "nu <= 0 or nu > 1";
            }
        }

        if (svm_type == svm_parameter.EPSILON_SVR) {
            if (param.p < 0) {
                return "p < 0";
            }
        }

        if (param.shrinking != 0 &&
                param.shrinking != 1) {
            return "shrinking != 0 and shrinking != 1";
        }

        if (param.probability != 0 &&
                param.probability != 1) {
            return "probability != 0 and probability != 1";
        }

        return null;
    }

    //OK
    public static int svm_check_probability_model(svm_model model) {
        if (((model.param.svm_type == svm_parameter.EPSILON_SVR || model.param.svm_type == svm_parameter.NU_SVR) &&
                model.probA != null)) {
            return 1;
        } else {
            return 0;
        }
    }

}
