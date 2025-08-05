const sass = require("gulp-sass")(require("sass"));
const postcss = require("gulp-postcss");
const gulp = require("gulp");
const sourcemaps = require("gulp-sourcemaps");
const autoprefixer = require("autoprefixer");
const rename = require("gulp-rename");

const rollup = require("rollup");
const babel = require("@rollup/plugin-babel");
const resolve = require("@rollup/plugin-node-resolve");
const commonjs = require("@rollup/plugin-commonjs");
const terser = require("@rollup/plugin-terser");

function compileSassSync(sassOptions, sassGlobPattern, dest) {
  return gulp.src(sassGlobPattern, {base: "."})
    .pipe(sourcemaps.init())
    .pipe(sass(sassOptions, true))
    .pipe(postcss([autoprefixer({ grid: true })]))
    .pipe(sourcemaps.write("./"))
    .pipe(rename(path => {
      path.dirname = "";
    }))
    .pipe(gulp.dest(dest));
}

const babelOptions =
  {
    "exclude": "node_modules/**",
    "presets": [
      ["@babel/preset-env", {
        "useBuiltIns": "usage",
        "corejs": "3",
        "debug": false,
        "targets": {
          "chrome": 65,
          "firefox": 52,
          "safari": 11,
          "ios": 12,
          "edge": 16
        }
      }]
    ],
    "babelHelpers": "bundled",
    "plugins": ["@babel/plugin-transform-class-properties"],
  }

// tasks
gulp.task("rollup-babel", () => rollup.rollup({
    input: "./src/main/resources/js/all.js",
    plugins: [
      resolve(),
      commonjs(),
      babel(babelOptions),
      terser(),
    ]
  }).then(bundle => {
    return bundle.write({
      file: "./src/main/resources/public/assets/static/js/fcs-bundle.js",
      format: "iife",
      sourcemap: true
    })
  })
);

gulp.task("build-document-styles", () => {
  const dest = "src/main/resources/document-assets";
  const sassGlobPattern = "src/main/resources/document-assets/scss/*.scss";
  const sassOptions = {
    outputStyle: "expanded",
    includePath: "src/main/resources/document-assets/scss"
  };

  return compileSassSync(sassOptions, sassGlobPattern, dest);
});

// copy FDS images into public/assets
gulp.task('copyFdsImages', () => {
  return gulp.src(['fivium-design-system-core/fds/static/images/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/static/fds/images'));
});

// copy FDS into public/assets
gulp.task("copyFdsResources", () => {
  return gulp.src(["fivium-design-system-core/fds/**/*"])
    .pipe(gulp.dest("src/main/resources/templates/fds"));
});

// copy govuk-frontend into public/assets
gulp.task("copyGovukResources", () => {
  return gulp.src(["fivium-design-system-core/node_modules/govuk-frontend/**/*"])
    .pipe(gulp.dest("src/main/resources/public/assets/govuk-frontend"));
});

// copy FDS bundle into public/assets
gulp.task("copyJs", () => {
  return gulp.src(["src/main/resources/templates/fds/static/js/**/*"])
    .pipe(gulp.dest("src/main/resources/public/assets/static/fds/js"));
});

// copy vendor JS into public/assets
gulp.task("copyVendorJs", () => {
  return gulp.src(["src/main/resources/templates/fds/vendor/**/*"])
    .pipe(gulp.dest("src/main/resources/public/assets/static/js/vendor"))
});

// Init all appropriate resources into projects public/assets
gulp.task("initFds", gulp.series(["copyFdsResources", "copyFdsImages", "copyGovukResources", "copyJs", "copyVendorJs"]))

gulp.task("sassCi", gulp.series(["initFds"], () => {
  const dest = "src/main/resources/public/assets/static/css";
  const sassGlobPattern = "src/main/resources/scss/*.scss";
  const sassOptions = {
    outputStyle: "compressed",
    includePath: "src/main/resources/scss"
  };

  return compileSassSync(sassOptions, sassGlobPattern, dest);
}));

gulp.task("buildAll", gulp.series(["sassCi", "rollup-babel", "build-document-styles"]));
