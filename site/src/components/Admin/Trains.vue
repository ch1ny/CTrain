<template>
	<div>
		<div class="head" style="margin-bottom: 10px">
			<el-pagination :total="total" layout="total, prev, pager, next, jumper" :current-page.sync="page" @current-change="changePage">
			</el-pagination>
		</div>
		<el-table :data="trainList" v-loading="loading" stripe border>
			<el-table-column prop="id" label="列车车次"></el-table-column>
			<el-table-column prop="type" label="列车类型"></el-table-column>
			<el-table-column label="操作">
				<template slot-scope="scope">
					<el-button type="primary" @click="details(scope.$index)"><i class="el-icon-tickets"></i>详细</el-button>
					<el-button type="danger"><i class="el-icon-delete"></i>删除</el-button>
				</template>
			</el-table-column>
			<div slot="append">
				<el-button @click="isAdding=true;" style="width: 100%; margin-bottom: 1px;" plain><i class="el-icon-plus"></i></el-button>
			</div>
		</el-table>

		<div class="others">
			<el-dialog title="增加列车" :visible.sync="isAdding" width="30%" @open="$refs.addForm?$refs.addForm.resetFields():''">
				<el-form :model="addForm" ref="addForm" :rules="addRules">
					<el-form-item prop="id">
						<el-input v-model="addForm.id" placeholder="请输入列车号" prefix-icon="iconfont icon-huochepiao"></el-input>
					</el-form-item>
					<el-form-item prop="type">
						<el-select v-model="addForm.type" placeholder="请选择列车类型" style="width: 100%">
							<el-option v-for="item in trainTypes" :key="item.value" :label="item.label" :value="item.value"></el-option>
						</el-select>
					</el-form-item>
				</el-form>
				<el-button type="primary" @click="add" :loading="adding">添加</el-button>
			</el-dialog>
		</div>
	</div>
</template>

<script>
import $ajax from "@/ajax";
export default {
	data() {
		return {
			loading: true,
			trainList: [],
			total: 0,
			page: 1,
			size: 10,
			isAdding: false,
			addForm: {
				id: "",
				type: "高铁",
			},
			addRules: {
				id: [
					{
						required: true,
						message: "不能为空哦",
						trigger: ["blur", "change"],
					},
					{
						pattern: /^[0-9a-zA-Z]+$/,
						message: "仅允许输入英文字母及数字",
						trigger: ["blur", "change"],
					},
				],
			},
			adding: false,
			trainTypes: [
				{ value: "高铁", label: "高铁" },
				{ value: "动车", label: "动车" },
				{ value: "直达", label: "直达" },
				{ value: "特快", label: "特快" },
				{ value: "快速", label: "快速" },
				{ value: "普快", label: "普快" },
				{ value: "普通", label: "普通" },
			],
		};
	},
	methods: {
		changePage() {
			this.getTrainList();
		},
		async getTrainList() {
			this.loading = true;
			let res = await $ajax.post(
				"/admin/getTrains",
				{
					pageNum: this.page,
					pageSize: this.size,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.trainList = res.data.data.trains;
				this.total = res.data.data.total;
			}
			this.loading = false;
		},
		add() {
			this.$refs.addForm.validate(async (valid) => {
				if (valid) {
					this.adding = true;
					let res = await $ajax.post(
						"/admin/addTrain",
						this.addForm,
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					if (res.data.code == 0) {
						this.getTrainList();
						this.$message({
							message: "添加成功！",
							type: "success",
							duration: 1000,
						});
						this.isAdding = false;
					} else {
						this.$message({
							message: "添加失败！" + res.data.data,
							type: "error",
							duration: 1000,
						});
					}
					this.adding = false;
				}
			});
		},
		details(index) {
			this.$router.push({
				path: "/admin/route/" + this.trainList[index].id,
			});
		},
	},
	created() {
		this.getTrainList();
	},
	watch: {
		$route(to, from) {
			if (
				to.path == "/admin/index" &&
				!from.path.match(/\/admin\/route/)
			) {
				this.page = 1;
				this.getTrainList();
			}
		},
	},
};
</script>