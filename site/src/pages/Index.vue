<template>
	<div>
		<Header></Header>
		<el-switch class="switch" v-model="darkStyle" active-color="dimgray" active-icon-class="el-icon-moon" inactive-color="gold" inactive-icon-class="el-icon-sunny" @change="themeChange"></el-switch>
		<el-container>
			<el-main style="text-align: center">
				<el-card class="cardBox" :style="{'background': darkStyle?'rgba(10, 10, 10, 0.7)':'rgba(250, 250, 250, 0.7)'}">
					<div slot="header" style="text-align: left">
						<span :style="{color:darkStyle?'white':''}">出行计划</span>
						<el-link @click="swap" :style="{position:'absolute', right:'5%', color:darkStyle?'white':''}"><i class="el-icon-refresh"></i></el-link>
					</div>
					<div>
						<el-form ref="form" :model="form" :rules="rules">
							<el-form-item prop="start">
								<el-autocomplete @keydown.enter.native="search" placeholder="请输入出发城市(支持拼音及首字母模糊搜索)" v-model="form.start" class="input" prefix-icon="el-icon-location-outline" :fetch-suggestions="querySearch" :trigger-on-focus="false"></el-autocomplete>
							</el-form-item>
							<el-form-item prop="dest">
								<el-autocomplete @keydown.enter.native="search" placeholder="请输入到达城市(支持拼音及首字母模糊搜索)" v-model="form.dest" class="input" prefix-icon="el-icon-location" :fetch-suggestions="querySearch" :trigger-on-focus="false"></el-autocomplete>
							</el-form-item>
						</el-form>
						<el-button type="primary" @click="search" :loading="loading" @mouseover.native="mouseHover" @mouseleave.native="mouseLeave" style="width: 30%; margin-top: 15px" :style="darkStyle?darkThemeBtnStyle:''">查询</el-button>
					</div>
				</el-card>
			</el-main>
		</el-container>
		<ribbons :darkStyle.sync="darkStyle" ref="ribbons"></ribbons>
	</div>
</template>

<script>
import $ajax from "../ajax";
import Header from "../components/Header.vue";
import ribbons from "../components/Ribbons.vue";

export default {
	components: {
		Header,
		ribbons,
	},
	data() {
		return {
			darkStyle: false,
			darkThemeBtnStyle:
				"background-color: gold; border-color: gold; color: #1f1f1f",
			loading: false,
			cities: [],
			form: {
				start: "",
				dest: "",
			},
			rules: {
				start: [
					{
						required: true,
						message: "请输入出发城市",
						trigger: ["blur", "change"],
					},
				],
				dest: [
					{
						required: true,
						message: "请输入到达城市",
						trigger: ["blur", "change"],
					},
				],
			},
		};
	},
	methods: {
		querySearch(queryString, cb) {
			let results = this.cities;
			results = queryString
				? results.filter(this.createFilter(queryString))
				: results;
			//cb是回调函数，返回筛选出的结果数据到输入框下面的输入列表
			cb(results);
		},
		createFilter(queryString) {
			let PY = require("js-pinyin");
			return (item) => {
				return (
					item.value.indexOf(queryString) === 0 ||
					PY.getFullChars(item.value)
						.toLowerCase()
						.indexOf(queryString.toLowerCase()) === 0 ||
					PY.getCamelChars(item.value).indexOf(
						queryString.toUpperCase()
					) === 0
				);
			};
		},
		themeChange() {
			this.$nextTick(() => {
				this.$refs.ribbons.themeChange();
			});
		},
		swap() {
			let temp = this.form.start;
			this.form.start = this.form.dest;
			this.form.dest = temp;
		},
		mouseHover() {
			if (this.darkStyle) {
				this.darkThemeBtnStyle =
					"background-color: yellow; border-color: yellow; color: #1f1f1f";
			}
		},
		mouseLeave() {
			if (this.darkStyle) {
				this.darkThemeBtnStyle =
					"background-color: gold; border-color: gold; color: #1f1f1f";
			}
		},
		search() {
			this.$refs.form.validate((valid) => {
				if (valid) {
					if (localStorage.getItem("token") == null) {
						this.$message({
							message: "请先登录",
							type: "error",
							duration: 1000,
						});
						return;
					}
					this.$router.push({
						path: "/search",
						query: { from: this.form.start, to: this.form.dest },
					});
				}
			});
		},
		async getCities() {
			let res = await $ajax.post("/service/getCities");
			let PY = require("js-pinyin");
			let QuickSort = (arr) => {
				if (arr.length <= 1) {
					return arr;
				}
				var pivotIndex = Math.floor(arr.length / 2);
				var pivot = arr.splice(pivotIndex, 1)[0];
				var left = [];
				var right = [];
				for (var i = 0; i < arr.length; i++) {
					if (PY.getFullChars(arr[i]) < PY.getFullChars(pivot)) {
						left.push(arr[i]);
					} else {
						right.push(arr[i]);
					}
				}
				return QuickSort(left).concat([pivot], QuickSort(right));
			};
			let data = QuickSort(res.data.data);
			for (let key in data) {
				this.cities.push({ value: data[key] });
			}
		},
	},
	async created() {
		this.getCities();
	},
};
</script>

<style scoped>
.switch {
	position: absolute;
	right: 5%;
	top: 15%;
}
.cardBox {
	width: 40%;
	padding-bottom: 50px;
	position: absolute;
	left: 30%;
	top: 25%;
}
.el-form-item >>> .el-form-item__error {
	left: 10%;
}
.input {
	width: 80%;
	margin-top: 3%;
}
</style>